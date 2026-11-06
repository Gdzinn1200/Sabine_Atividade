package br.edu.cesumar.agendapro.dominio.regras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos CT12 a CT26: disponibilidade e gravacao do agendamento. */
class RegrasAgendaTest {

    private static final Long PROF = 1L;
    private static final Long CLIENTE = 1L;

    /** Uma quarta-feira futura, para o teste nao depender do dia em que roda. */
    private static LocalDate proximaQuarta() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    private final LocalDate dia = proximaQuarta();
    private final LocalDateTime agora = LocalDateTime.of(dia.minusDays(1), LocalTime.of(10, 0));

    private final List<RegrasAgenda.Faixa> faixas = List.of(
            new RegrasAgenda.Faixa(PROF, RegrasAgenda.diaSemana(dia), LocalTime.of(8, 0), LocalTime.of(18, 0)));

    private final List<RegrasAgenda.Bloqueio> bloqueios = List.of(
            new RegrasAgenda.Bloqueio(PROF, dia, LocalTime.of(12, 0), LocalTime.of(13, 0), "Almoco"));

    private final List<RegrasAgenda.Compromisso> ocupados = List.of(
            new RegrasAgenda.Compromisso(10L, PROF, 2L, dia, LocalTime.of(9, 0), LocalTime.of(9, 40),
                    SituacaoAgendamento.CONFIRMADO),
            new RegrasAgenda.Compromisso(11L, PROF, 3L, dia, LocalTime.of(16, 0), LocalTime.of(16, 40),
                    SituacaoAgendamento.CANCELADO));

    private final RegrasAgenda.Agenda estado = new RegrasAgenda.Agenda(faixas, bloqueios, ocupados);

    private static boolean temRegra(List<ErroValidacao> erros, String regra) {
        return erros.stream().anyMatch(e -> regra.equals(e.regra()));
    }

    @Test
    @DisplayName("CT12 - RN01: sobreposicao de horario e detectada")
    void ct12() {
        assertTrue(RegrasAgenda.conflito(PROF, dia, LocalTime.of(9, 20), LocalTime.of(10, 0),
                ocupados, null).isPresent());
    }

    @Test
    @DisplayName("CT13 - RN01: atendimentos encostados nao conflitam")
    void ct13() {
        assertTrue(RegrasAgenda.conflito(PROF, dia, LocalTime.of(9, 40), LocalTime.of(10, 20),
                ocupados, null).isEmpty());
    }

    @Test
    @DisplayName("CT14 - RN01: atendimento cancelado libera o horario")
    void ct14() {
        assertTrue(RegrasAgenda.conflito(PROF, dia, LocalTime.of(16, 0), LocalTime.of(16, 40),
                ocupados, null).isEmpty());
    }

    @Test
    @DisplayName("CT15 - RN02: horario fora da faixa de atendimento e recusado")
    void ct15() {
        assertFalse(RegrasAgenda.dentroDoHorario(PROF, dia, LocalTime.of(7, 0), LocalTime.of(7, 40), faixas));
        assertTrue(RegrasAgenda.dentroDoHorario(PROF, dia, LocalTime.of(14, 0), LocalTime.of(14, 40), faixas));
    }

    @Test
    @DisplayName("CT16 - RN02: bloqueio de agenda e detectado")
    void ct16() {
        assertTrue(RegrasAgenda.bloqueio(PROF, dia, LocalTime.of(12, 20), LocalTime.of(13, 0),
                bloqueios).isPresent());
    }

    @Test
    @DisplayName("CT17 - RF07: a grade marca ocupados, bloqueados e livres")
    void ct17() {
        List<RegrasAgenda.Horario> grade = RegrasAgenda.horariosDisponiveis(PROF, dia, 40, estado, agora, null);
        assertFalse(grade.isEmpty());
        assertTrue(grade.stream().anyMatch(h -> h.inicio().equals(LocalTime.of(9, 0)) && !h.livre()));
        assertTrue(grade.stream().anyMatch(h -> h.inicio().equals(LocalTime.of(12, 0)) && !h.livre()));
        assertTrue(grade.stream().anyMatch(h -> h.inicio().equals(LocalTime.of(14, 0)) && h.livre()));
    }

    @Test
    @DisplayName("CT18 - RN04: horarios ja passados nao sao oferecidos")
    void ct18() {
        LocalDateTime depois = LocalDateTime.of(dia, LocalTime.of(15, 0));
        List<RegrasAgenda.Horario> grade = RegrasAgenda.horariosDisponiveis(PROF, dia, 40, estado, depois, null);
        assertTrue(grade.stream()
                .filter(h -> h.inicio().isBefore(LocalTime.of(15, 0)))
                .noneMatch(RegrasAgenda.Horario::livre));
    }

    @Test
    @DisplayName("CT19 - RN06: profissional que nao executa o servico e recusado")
    void ct19() {
        RegrasAgenda.Pedido pedido = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, false, dia, LocalTime.of(14, 0), 40);
        assertTrue(temRegra(RegrasAgenda.validarAgendamento(pedido, estado, agora), "RN06"));
    }

    @Test
    @DisplayName("CT20 - RN08: o quarto agendamento futuro do cliente e recusado")
    void ct20() {
        List<RegrasAgenda.Compromisso> tres = List.of(
                new RegrasAgenda.Compromisso(20L, PROF, CLIENTE, dia, LocalTime.of(8, 0),
                        LocalTime.of(8, 40), SituacaoAgendamento.AGENDADO),
                new RegrasAgenda.Compromisso(21L, PROF, CLIENTE, dia, LocalTime.of(10, 0),
                        LocalTime.of(10, 40), SituacaoAgendamento.AGENDADO),
                new RegrasAgenda.Compromisso(22L, PROF, CLIENTE, dia, LocalTime.of(11, 0),
                        LocalTime.of(11, 40), SituacaoAgendamento.CONFIRMADO));
        assertEquals(3, RegrasAgenda.agendamentosFuturosAtivos(CLIENTE, tres, agora, null));

        RegrasAgenda.Agenda cheia = new RegrasAgenda.Agenda(faixas, List.of(), tres);
        RegrasAgenda.Pedido quarto = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, true, dia, LocalTime.of(14, 0), 40);
        assertTrue(temRegra(RegrasAgenda.validarAgendamento(quarto, cheia, agora), "RN08"));
    }

    @Test
    @DisplayName("CT21 - RN10: faltas na janela de 90 dias geram bloqueio")
    void ct21() {
        LocalDate hoje = LocalDate.now();
        List<RegrasAgenda.Compromisso> faltas = List.of(
                falta(30L, hoje.minusDays(10)),
                falta(31L, hoje.minusDays(40)),
                falta(32L, hoje.minusDays(80)),
                falta(33L, hoje.minusDays(200)));
        assertEquals(3, RegrasAgenda.faltasRecentes(CLIENTE, faltas, hoje));
        assertTrue(RegrasAgenda.deveBloquearPorFaltas(CLIENTE, faltas, hoje));
    }

    @Test
    @DisplayName("CT22 - RN10: cliente bloqueado nao consegue agendar")
    void ct22() {
        RegrasAgenda.Pedido pedido = new RegrasAgenda.Pedido(null, CLIENTE, true,
                PROF, true, true, dia, LocalTime.of(14, 0), 40);
        assertTrue(temRegra(RegrasAgenda.validarAgendamento(pedido, estado, agora), "RN10"));
    }

    @Test
    @DisplayName("CT23 - RN04: agendamento em data passada e recusado")
    void ct23() {
        RegrasAgenda.Pedido pedido = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, true, dia.minusDays(10), LocalTime.of(14, 0), 40);
        assertTrue(temRegra(RegrasAgenda.validarAgendamento(pedido, estado, agora), "RN04"));
    }

    @Test
    @DisplayName("CT24 - RN01: agendamento sobreposto e recusado na gravacao")
    void ct24() {
        RegrasAgenda.Pedido pedido = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, true, dia, LocalTime.of(9, 20), 40);
        assertTrue(temRegra(RegrasAgenda.validarAgendamento(pedido, estado, agora), "RN01"));
    }

    @Test
    @DisplayName("CT25 - RF08: agendamento valido passa em todas as regras")
    void ct25() {
        RegrasAgenda.Pedido pedido = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, true, dia, LocalTime.of(14, 0), 40);
        var erros = RegrasAgenda.validarAgendamento(pedido, estado, agora);
        assertTrue(erros.isEmpty(), () -> "nao deveria haver erros: " + erros);
    }

    @Test
    @DisplayName("CT26 - RF05 e RN02: faixa invertida e faixa sobreposta sao recusadas")
    void ct26() {
        var invertida = RegrasProfissional.validarFaixa(4, LocalTime.of(10, 0), LocalTime.of(9, 0), List.of());
        assertEquals(1, invertida.size());

        var existentes = List.of(new RegrasProfissional.FaixaExistente(1L, 4,
                LocalTime.of(8, 0), LocalTime.of(18, 0)));
        var sobreposta = RegrasProfissional.validarFaixa(4, LocalTime.of(17, 0), LocalTime.of(19, 0), existentes);
        assertEquals(1, sobreposta.size());
        assertEquals("RN02", sobreposta.get(0).regra());
    }

    private RegrasAgenda.Compromisso falta(Long id, LocalDate quando) {
        return new RegrasAgenda.Compromisso(id, PROF, CLIENTE, quando, LocalTime.of(9, 0),
                LocalTime.of(9, 40), SituacaoAgendamento.NAO_COMPARECEU);
    }
}
