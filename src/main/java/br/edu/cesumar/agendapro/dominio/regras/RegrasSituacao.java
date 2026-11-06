package br.edu.cesumar.agendapro.dominio.regras;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Regras do ciclo de vida do atendimento. Classe de dominio puro.
 *
 * Regras aplicadas:
 *  - RN05: o cliente so cancela ou remarca com 4 horas de antecedencia
 *  - RN09: a situacao segue Agendado -> Confirmado -> Em atendimento -> Concluido;
 *          Concluido, Cancelado e Nao compareceu sao finais
 */
public final class RegrasSituacao {

    public static final int HORAS_ANTECEDENCIA = 4;

    private RegrasSituacao() {
    }

    /** Quem pediu a operacao. Enquanto nao ha autenticacao, a tela informa o perfil. */
    public enum Perfil {
        ADMINISTRADOR, PROFISSIONAL, CLIENTE
    }

    /** RN09: situacoes que podem ser alcancadas a partir da situacao atual. */
    public static Set<SituacaoAgendamento> proximas(SituacaoAgendamento atual) {
        return switch (atual) {
            case AGENDADO -> EnumSet.of(SituacaoAgendamento.CONFIRMADO,
                    SituacaoAgendamento.EM_ATENDIMENTO,
                    SituacaoAgendamento.CANCELADO,
                    SituacaoAgendamento.NAO_COMPARECEU);
            case CONFIRMADO -> EnumSet.of(SituacaoAgendamento.EM_ATENDIMENTO,
                    SituacaoAgendamento.CANCELADO,
                    SituacaoAgendamento.NAO_COMPARECEU);
            case EM_ATENDIMENTO -> EnumSet.of(SituacaoAgendamento.CONCLUIDO);
            default -> EnumSet.noneOf(SituacaoAgendamento.class);
        };
    }

    public static boolean transicaoPermitida(SituacaoAgendamento de, SituacaoAgendamento para) {
        return proximas(de).contains(para);
    }

    /** RN09: valida a mudanca de situacao e devolve os erros encontrados. */
    public static List<ErroValidacao> validarTransicao(SituacaoAgendamento de, SituacaoAgendamento para) {
        List<ErroValidacao> erros = new ArrayList<>();
        if (de == null || para == null) {
            erros.add(ErroValidacao.de("situacao", "Informe a nova situacao do atendimento."));
            return erros;
        }
        if (de.isFinal()) {
            erros.add(ErroValidacao.de("situacao",
                    "O atendimento esta em situacao final (" + de.getRotulo() + ") e nao pode ser alterado.",
                    "RN09"));
        } else if (!transicaoPermitida(de, para)) {
            erros.add(ErroValidacao.de("situacao",
                    "Nao e possivel passar de " + de.getRotulo() + " para " + para.getRotulo() + ".",
                    "RN09"));
        }
        return erros;
    }

    /**
     * RN05 e RN09: valida o cancelamento. O administrador e o profissional cancelam
     * a qualquer momento; o cliente precisa de antecedencia minima.
     */
    public static List<ErroValidacao> validarCancelamento(SituacaoAgendamento atual,
                                                          LocalDateTime inicioAtendimento,
                                                          LocalDateTime agora,
                                                          Perfil solicitante,
                                                          String motivo) {
        List<ErroValidacao> erros = new ArrayList<>();
        if (atual != null && atual.isFinal()) {
            erros.add(ErroValidacao.de("situacao",
                    "O atendimento esta em situacao final (" + atual.getRotulo() + ") e nao pode ser cancelado.",
                    "RN09"));
        }
        if (motivo == null || motivo.isBlank()) {
            erros.add(ErroValidacao.de("motivo", "Informe o motivo do cancelamento."));
        }
        if (solicitante == Perfil.CLIENTE && inicioAtendimento != null && agora != null) {
            long horas = Duration.between(agora, inicioAtendimento).toMinutes() / 60;
            if (horas < HORAS_ANTECEDENCIA) {
                erros.add(ErroValidacao.de("antecedencia",
                        "O cancelamento pelo cliente exige " + HORAS_ANTECEDENCIA
                                + " horas de antecedencia. Procure o estabelecimento.", "RN05"));
            }
        }
        return erros;
    }

    public static void validarCancelamentoOuFalhar(SituacaoAgendamento atual,
                                                   LocalDateTime inicioAtendimento,
                                                   LocalDateTime agora,
                                                   Perfil solicitante,
                                                   String motivo) {
        List<ErroValidacao> erros = validarCancelamento(atual, inicioAtendimento, agora, solicitante, motivo);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    public static void validarTransicaoOuFalhar(SituacaoAgendamento de, SituacaoAgendamento para) {
        List<ErroValidacao> erros = validarTransicao(de, para);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
}
