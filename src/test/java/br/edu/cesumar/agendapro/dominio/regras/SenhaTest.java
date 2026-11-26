package br.edu.cesumar.agendapro.dominio.regras;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos CT42 a CT48: guarda da senha e token de recuperacao (RNF01 e RF18). */
class SenhaTest {

    @Test
    @DisplayName("CT42 - a senha nao fica guardada em texto")
    void senhaNaoFicaEmTexto() {
        String registro = Senha.cifrar("minhaSenha123");
        assertTrue(Senha.registroCifrado(registro));
        assertFalse(registro.contains("minhaSenha123"));
    }

    @Test
    @DisplayName("CT43 - senha correta e aceita")
    void senhaCorretaEAceita() {
        assertTrue(Senha.confere("minhaSenha123", Senha.cifrar("minhaSenha123")));
    }

    @Test
    @DisplayName("CT44 - senha errada e recusada")
    void senhaErradaERecusada() {
        assertFalse(Senha.confere("minhasenha123", Senha.cifrar("minhaSenha123")));
    }

    @Test
    @DisplayName("CT45 - cada cadastro usa um sal diferente")
    void salDiferentePorCadastro() {
        assertNotEquals(Senha.cifrar("minhaSenha123"), Senha.cifrar("minhaSenha123"));
    }

    @Test
    @DisplayName("CT46 - senha curta e recusada")
    void senhaCurtaERecusada() {
        assertFalse(Senha.validar("123", "123").isEmpty());
    }

    @Test
    @DisplayName("CT47 - confirmacao diferente e recusada")
    void confirmacaoDiferenteERecusada() {
        assertFalse(Senha.validar("123456", "654321").isEmpty());
    }

    @Test
    @DisplayName("CT48 - token de recuperacao nao se repete")
    void tokenNaoSeRepete() {
        assertNotEquals(Senha.tokenAleatorio(), Senha.tokenAleatorio());
    }
}
