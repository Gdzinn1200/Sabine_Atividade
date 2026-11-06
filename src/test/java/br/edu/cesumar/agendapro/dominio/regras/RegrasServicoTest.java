package br.edu.cesumar.agendapro.dominio.regras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos de teste CT07 a CT10 do cadastro de servicos (RF04). */
class RegrasServicoTest {

    private static final List<RegrasServico.ServicoExistente> BASE = List.of(
            new RegrasServico.ServicoExistente(1L, "Corte feminino", true));

    @Test
    @DisplayName("CT07 - servico com dados validos e aceito")
    void ct07() {
        var erros = RegrasServico.validar(null, "Coloracao", 90, new BigDecimal("180.00"), BASE);
        assertTrue(erros.isEmpty(), () -> "nao deveria haver erros: " + erros);
    }

    @Test
    @DisplayName("CT08 - RN03: duracao zero ou ausente e recusada")
    void ct08() {
        var semDuracao = RegrasServico.validar(null, "Teste", null, BigDecimal.TEN, BASE);
        var duracaoZero = RegrasServico.validar(null, "Teste", 0, BigDecimal.TEN, BASE);
        assertEquals("RN03", semDuracao.get(0).regra());
        assertEquals("RN03", duracaoZero.get(0).regra());
    }

    @Test
    @DisplayName("CT09 - RN14: nome repetido de servico ativo e recusado")
    void ct09() {
        var erros = RegrasServico.validar(null, "corte feminino", 40, new BigDecimal("70.00"), BASE);
        assertEquals(1, erros.size());
        assertEquals("RN14", erros.get(0).regra());
    }

    @Test
    @DisplayName("CT10 - RN03: horario final sai da duracao do servico")
    void ct10() {
        assertEquals(LocalTime.of(10, 30), RegrasServico.horarioFinal(LocalTime.of(9, 0), 90));
        assertEquals(LocalTime.of(9, 40), RegrasServico.horarioFinal(LocalTime.of(9, 0), 40));
    }

    @Test
    @DisplayName("CT11 - preco negativo e recusado")
    void ct11() {
        var erros = RegrasServico.validar(null, "Teste preco", 30, new BigDecimal("-1.00"), BASE);
        assertEquals(1, erros.size());
        assertEquals("preco", erros.get(0).campo());
    }
}
