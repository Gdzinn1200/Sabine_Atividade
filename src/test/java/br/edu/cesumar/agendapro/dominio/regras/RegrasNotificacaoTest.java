package br.edu.cesumar.agendapro.dominio.regras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos CT49 a CT56: lembrete de 24 horas e avisos de situacao (RN11, RF14, RF15). */
class RegrasNotificacaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 27, 8, 0);

    private RegrasNotificacao.Candidato candidato(long id, LocalDateTime inicio,
                                                  SituacaoAgendamento situacao,
                                                  boolean jaRecebeu) {
        return new RegrasNotificacao.Candidato(id, inicio, situacao, jaRecebeu);
    }

    @Test
    @DisplayName("CT49 - lembrete sai nas 24 horas anteriores")
    void lembreteDentroDaJanela() {
        assertTrue(RegrasNotificacao.deveEnviarLembrete(
                candidato(1L, AGORA.plusHours(20), SituacaoAgendamento.CONFIRMADO, false), AGORA));
    }

    @Test
    @DisplayName("CT50 - lembrete nao se repete")
    void lembreteNaoSeRepete() {
        assertFalse(RegrasNotificacao.deveEnviarLembrete(
                candidato(2L, AGORA.plusHours(20), SituacaoAgendamento.CONFIRMADO, true), AGORA));
    }

    @Test
    @DisplayName("CT51 - atendimento distante ainda nao gera lembrete")
    void atendimentoDistante() {
        assertFalse(RegrasNotificacao.deveEnviarLembrete(
                candidato(3L, AGORA.plusDays(5), SituacaoAgendamento.AGENDADO, false), AGORA));
    }

    @Test
    @DisplayName("CT52 - atendimento cancelado nao gera lembrete")
    void atendimentoCancelado() {
        assertFalse(RegrasNotificacao.deveEnviarLembrete(
                candidato(4L, AGORA.plusHours(20), SituacaoAgendamento.CANCELADO, false), AGORA));
    }

    @Test
    @DisplayName("CT53 - atendimento passado nao gera lembrete")
    void atendimentoPassado() {
        assertFalse(RegrasNotificacao.deveEnviarLembrete(
                candidato(5L, AGORA.minusHours(2), SituacaoAgendamento.CONFIRMADO, false), AGORA));
    }

    @Test
    @DisplayName("CT54 - rotina separa apenas quem precisa de lembrete")
    void rotinaSeparaCandidatos() {
        List<Long> fila = RegrasNotificacao.lembretesAEnviar(List.of(
                candidato(1L, AGORA.plusHours(20), SituacaoAgendamento.CONFIRMADO, false),
                candidato(2L, AGORA.plusHours(20), SituacaoAgendamento.CONFIRMADO, true),
                candidato(3L, AGORA.plusDays(5), SituacaoAgendamento.AGENDADO, false),
                candidato(4L, AGORA.plusHours(20), SituacaoAgendamento.CANCELADO, false)), AGORA);
        assertEquals(List.of(1L), fila);
    }

    @Test
    @DisplayName("CT55 - cada situacao tem o seu tipo de aviso")
    void tipoDeAvisoPorSituacao() {
        assertEquals(TipoNotificacao.CANCELAMENTO,
                RegrasNotificacao.tipoPara(SituacaoAgendamento.CANCELADO));
        assertEquals(TipoNotificacao.CONFIRMACAO,
                RegrasNotificacao.tipoPara(SituacaoAgendamento.CONFIRMADO));
    }

    @Test
    @DisplayName("CT56 - destinatario sem e-mail nao recebe aviso")
    void destinatarioSemEmail() {
        assertTrue(RegrasNotificacao.podeNotificar("marina@email.com"));
        assertFalse(RegrasNotificacao.podeNotificar(""));
        assertFalse(RegrasNotificacao.podeNotificar(null));
    }
}
