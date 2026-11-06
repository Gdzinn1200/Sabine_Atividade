package br.edu.cesumar.agendapro.dominio.regras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos de teste CT01 a CT05 do cadastro de clientes (RF01). */
class RegrasClienteTest {

    private static final List<RegrasCliente.ClienteExistente> BASE = List.of(
            new RegrasCliente.ClienteExistente(1L, "marina@email.com", "(42) 99876-1122", true),
            new RegrasCliente.ClienteExistente(2L, "rafael@email.com", "(42) 99712-3040", false));

    @Test
    @DisplayName("CT01 - cliente com dados validos e aceito")
    void ct01() {
        var erros = RegrasCliente.validar(null, "Camila Reis", "(42) 99500-7711",
                "camila@email.com", LocalDate.of(1999, 7, 25), BASE);
        assertTrue(erros.isEmpty(), () -> "nao deveria haver erros: " + erros);
    }

    @Test
    @DisplayName("CT02 - nome e telefone obrigatorios sao recusados quando vazios")
    void ct02() {
        var erros = RegrasCliente.validar(null, "", "", "", null, BASE);
        assertEquals(2, erros.size());
        assertTrue(erros.stream().anyMatch(e -> e.campo().equals("nome")));
        assertTrue(erros.stream().anyMatch(e -> e.campo().equals("telefone")));
    }

    @Test
    @DisplayName("CT03 - RN07: e-mail repetido de cliente ativo e recusado")
    void ct03() {
        var erros = RegrasCliente.validar(null, "Outra Pessoa", "(42) 90000-0001",
                "marina@email.com", null, BASE);
        assertEquals(1, erros.size());
        assertEquals("RN07", erros.get(0).regra());
    }

    @Test
    @DisplayName("CT04 - RN07: telefone de cliente inativo pode ser reutilizado")
    void ct04() {
        var erros = RegrasCliente.validar(null, "Novo Cliente", "(42) 99712-3040",
                "novo@email.com", null, BASE);
        assertTrue(erros.isEmpty(), () -> "cliente inativo nao bloqueia: " + erros);
    }

    @Test
    @DisplayName("CT05 - edicao do proprio registro nao acusa duplicidade")
    void ct05() {
        var erros = RegrasCliente.validar(1L, "Marina Souza", "(42) 99876-1122",
                "marina@email.com", null, BASE);
        assertTrue(erros.isEmpty(), () -> "o proprio registro nao conflita: " + erros);
    }

    @Test
    @DisplayName("CT06 - e-mail em formato invalido e recusado")
    void ct06() {
        var erros = RegrasCliente.validar(null, "Teste Formato", "(42) 90000-0002",
                "email-invalido", null, BASE);
        assertEquals(1, erros.size());
        assertEquals("email", erros.get(0).campo());
    }
}
