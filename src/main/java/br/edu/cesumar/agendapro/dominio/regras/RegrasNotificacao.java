package br.edu.cesumar.agendapro.dominio.regras;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras do envio de mensagens ao cliente. Classe de dominio puro.
 *
 * Regras aplicadas:
 *  - RF14: confirmacao ao criar, alterar e cancelar o agendamento
 *  - RF15: lembrete do atendimento no dia anterior
 *  - RN11: o lembrete e enviado uma unica vez, 24 horas antes, e apenas para
 *    agendamentos com situacao Agendado ou Confirmado
 */
public final class RegrasNotificacao {

    public static final int HORAS_ANTECEDENCIA_LEMBRETE = 24;

    private RegrasNotificacao() {
    }

    /** Um agendamento candidato a receber lembrete. */
    public record Candidato(Long idAgendamento, LocalDateTime inicio,
                            SituacaoAgendamento situacao, boolean jaRecebeuLembrete) {
    }

    /** RN11: este agendamento deve receber lembrete agora? */
    public static boolean deveEnviarLembrete(Candidato candidato, LocalDateTime agora) {
        if (candidato == null || candidato.inicio() == null || candidato.situacao() == null) {
            return false;
        }
        if (candidato.jaRecebeuLembrete()) {
            return false;
        }
        if (candidato.situacao() != SituacaoAgendamento.AGENDADO
                && candidato.situacao() != SituacaoAgendamento.CONFIRMADO) {
            return false;
        }
        if (!candidato.inicio().isAfter(agora)) {
            return false;
        }
        long horas = Duration.between(agora, candidato.inicio()).toHours();
        return horas <= HORAS_ANTECEDENCIA_LEMBRETE;
    }

    /** Os agendamentos que devem receber lembrete nesta execucao da rotina. */
    public static List<Long> lembretesAEnviar(List<Candidato> candidatos, LocalDateTime agora) {
        List<Long> ids = new ArrayList<>();
        for (Candidato c : candidatos) {
            if (deveEnviarLembrete(c, agora)) {
                ids.add(c.idAgendamento());
            }
        }
        return ids;
    }

    /** RF14: a mudanca de situacao gera aviso ao cliente? */
    public static TipoNotificacao tipoPara(SituacaoAgendamento nova) {
        if (nova == SituacaoAgendamento.CANCELADO) {
            return TipoNotificacao.CANCELAMENTO;
        }
        return null;
    }

    /** O cliente so recebe mensagem quando ha endereco de e-mail cadastrado. */
    public static boolean podeNotificar(String destinatario) {
        return destinatario != null && !destinatario.isBlank() && destinatario.contains("@");
    }
}
