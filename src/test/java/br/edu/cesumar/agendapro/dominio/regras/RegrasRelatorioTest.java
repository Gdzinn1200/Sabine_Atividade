package br.edu.cesumar.agendapro.dominio.regras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos CT57 a CT65: relatorio de atendimentos (RF16 e RN13). */
class RegrasRelatorioTest {

    private static final LocalDate DE = LocalDate.of(2026, 11, 1);
    private static final LocalDate ATE = LocalDate.of(2026, 11, 30);

    private static final List<RegrasRelatorio.Atendimento> DADOS = List.of(
            new RegrasRelatorio.Atendimento(LocalDate.of(2026, 11, 3), "Bruno", "Corte",
                    SituacaoAgendamento.CONCLUIDO, new BigDecimal("45.00")),
            new RegrasRelatorio.Atendimento(LocalDate.of(2026, 11, 4), "Bruno", "Barba",
                    SituacaoAgendamento.CONCLUIDO, new BigDecimal("30.00")),
            new RegrasRelatorio.Atendimento(LocalDate.of(2026, 11, 5), "Bruno", "Corte",
                    SituacaoAgendamento.CANCELADO, new BigDecimal("45.00")),
            new RegrasRelatorio.Atendimento(LocalDate.of(2026, 11, 6), "Daniela", "Corte",
                    SituacaoAgendamento.NAO_COMPARECEU, new BigDecimal("45.00")),
            new RegrasRelatorio.Atendimento(LocalDate.of(2026, 12, 2), "Daniela", "Corte",
                    SituacaoAgendamento.CONCLUIDO, new BigDecimal("45.00")));

    private List<RegrasRelatorio.Atendimento> doPeriodo() {
        return RegrasRelatorio.noPeriodo(DADOS, DE, ATE);
    }

    @Test
    @DisplayName("CT57 - relatorio considera apenas o periodo pedido")
    void apenasOPeriodo() {
        assertEquals(4, doPeriodo().size());
    }

    @Test
    @DisplayName("CT58 - faturamento soma somente atendimentos concluidos")
    void faturamentoSomenteConcluidos() {
        assertEquals(0, RegrasRelatorio.resumir(doPeriodo())
                .faturamento().compareTo(new BigDecimal("75.00")));
    }

    @Test
    @DisplayName("CT59 - resumo separa concluidos, cancelados e faltas")
    void resumoSeparaSituacoes() {
        RegrasRelatorio.Resumo resumo = RegrasRelatorio.resumir(doPeriodo());
        assertEquals(4, resumo.total());
        assertEquals(2, resumo.concluidos());
        assertEquals(1, resumo.cancelados());
        assertEquals(1, resumo.faltas());
    }

    @Test
    @DisplayName("CT60 - taxa de comparecimento desconsidera cancelados")
    void taxaDeComparecimento() {
        // 2 concluidos e 1 falta: o cancelado liberou o horario e nao entra na conta.
        assertEquals(67, RegrasRelatorio.resumir(doPeriodo()).taxaComparecimento());
    }

    @Test
    @DisplayName("CT61 - totais agrupados por profissional")
    void totaisPorProfissional() {
        List<RegrasRelatorio.Linha> linhas = RegrasRelatorio.porProfissional(doPeriodo());
        assertEquals(2, linhas.size());
        assertTrue(linhas.stream().anyMatch(l -> "Bruno".equals(l.rotulo()) && l.total() == 3));
    }

    @Test
    @DisplayName("CT62 - totais agrupados por servico")
    void totaisPorServico() {
        assertTrue(RegrasRelatorio.porServico(doPeriodo()).stream()
                .anyMatch(l -> "Corte".equals(l.rotulo()) && l.total() == 3));
    }

    @Test
    @DisplayName("CT63 - periodo invertido e recusado")
    void periodoInvertido() {
        assertFalse(RegrasRelatorio.validarPeriodo(ATE, DE).isEmpty());
    }

    @Test
    @DisplayName("CT64 - periodo acima de 365 dias e recusado")
    void periodoMuitoLongo() {
        assertFalse(RegrasRelatorio.validarPeriodo(DE, DE.plusDays(400)).isEmpty());
    }

    @Test
    @DisplayName("CT65 - periodo valido e aceito")
    void periodoValido() {
        assertTrue(RegrasRelatorio.validarPeriodo(DE, ATE).isEmpty());
    }
}
