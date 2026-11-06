package br.edu.cesumar.agendapro.dominio.regras;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos CT27 a CT33: ciclo de vida do atendimento e cancelamento. */
class RegrasSituacaoTest {

    private final LocalDateTime agora = LocalDateTime.of(2026, 11, 10, 8, 0);

    private static boolean temRegra(List<ErroValidacao> erros, String regra) {
        return erros.stream().anyMatch(e -> regra.equals(e.regra()));
    }

    private static boolean temCampo(List<ErroValidacao> erros, String campo) {
        return erros.stream().anyMatch(e -> campo.equals(e.campo()));
    }

    @Test
    @DisplayName("CT27 - RN09: a sequencia prevista e permitida")
    void ct27() {
        assertTrue(RegrasSituacao.transicaoPermitida(SituacaoAgendamento.AGENDADO,
                SituacaoAgendamento.CONFIRMADO));
        assertTrue(RegrasSituacao.transicaoPermitida(SituacaoAgendamento.CONFIRMADO,
                SituacaoAgendamento.EM_ATENDIMENTO));
        assertTrue(RegrasSituacao.transicaoPermitida(SituacaoAgendamento.EM_ATENDIMENTO,
                SituacaoAgendamento.CONCLUIDO));
    }

    @Test
    @DisplayName("CT28 - RN09: salto de Agendado para Concluido e recusado")
    void ct28() {
        assertFalse(RegrasSituacao.transicaoPermitida(SituacaoAgendamento.AGENDADO,
                SituacaoAgendamento.CONCLUIDO));
        assertTrue(temRegra(RegrasSituacao.validarTransicao(SituacaoAgendamento.AGENDADO,
                SituacaoAgendamento.CONCLUIDO), "RN09"));
    }

    @Test
    @DisplayName("CT29 - RN09: situacao final nao pode ser alterada")
    void ct29() {
        assertTrue(SituacaoAgendamento.CONCLUIDO.isFinal());
        assertTrue(SituacaoAgendamento.CANCELADO.isFinal());
        assertTrue(SituacaoAgendamento.NAO_COMPARECEU.isFinal());
        assertTrue(temRegra(RegrasSituacao.validarTransicao(SituacaoAgendamento.CONCLUIDO,
                SituacaoAgendamento.CONFIRMADO), "RN09"));
    }

    @Test
    @DisplayName("CT30 - RN05: cliente nao cancela com menos de 4 horas")
    void ct30() {
        var erros = RegrasSituacao.validarCancelamento(SituacaoAgendamento.CONFIRMADO,
                agora.plusHours(2), agora, RegrasSituacao.Perfil.CLIENTE, "Solicitacao do cliente");
        assertTrue(temRegra(erros, "RN05"));
    }

    @Test
    @DisplayName("CT31 - RN05: cliente cancela com mais de 4 horas")
    void ct31() {
        var erros = RegrasSituacao.validarCancelamento(SituacaoAgendamento.CONFIRMADO,
                agora.plusHours(6), agora, RegrasSituacao.Perfil.CLIENTE, "Solicitacao do cliente");
        assertTrue(erros.isEmpty(), () -> "nao deveria haver erros: " + erros);
    }

    @Test
    @DisplayName("CT32 - RN05: o estabelecimento cancela a qualquer momento")
    void ct32() {
        var erros = RegrasSituacao.validarCancelamento(SituacaoAgendamento.CONFIRMADO,
                agora.plusHours(2), agora, RegrasSituacao.Perfil.ADMINISTRADOR,
                "Indisponibilidade do profissional");
        assertTrue(erros.isEmpty(), () -> "nao deveria haver erros: " + erros);
    }

    @Test
    @DisplayName("CT33 - RF10: o motivo do cancelamento e obrigatorio")
    void ct33() {
        var erros = RegrasSituacao.validarCancelamento(SituacaoAgendamento.AGENDADO,
                agora.plusHours(6), agora, RegrasSituacao.Perfil.ADMINISTRADOR, "  ");
        assertTrue(temCampo(erros, "motivo"));
    }
}
