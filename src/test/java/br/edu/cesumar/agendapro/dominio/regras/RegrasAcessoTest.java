package br.edu.cesumar.agendapro.dominio.regras;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos CT34 a CT41: acesso por perfil (RNF02 e RN13). */
class RegrasAcessoTest {

    @Test
    @DisplayName("CT34 - administrador abre os relatorios")
    void administradorAbreRelatorios() {
        assertTrue(RegrasAcesso.permitido(Perfil.ADMINISTRADOR, "/relatorios"));
    }

    @Test
    @DisplayName("CT35 - cliente nao abre os relatorios")
    void clienteNaoAbreRelatorios() {
        assertFalse(RegrasAcesso.permitido(Perfil.CLIENTE, "/relatorios"));
    }

    @Test
    @DisplayName("CT36 - profissional abre a agenda")
    void profissionalAbreAgenda() {
        assertTrue(RegrasAcesso.permitido(Perfil.PROFISSIONAL, "/agenda"));
    }

    @Test
    @DisplayName("CT37 - profissional nao administra usuarios")
    void profissionalNaoAbreUsuarios() {
        assertFalse(RegrasAcesso.permitido(Perfil.PROFISSIONAL, "/usuarios"));
    }

    @Test
    @DisplayName("CT38 - cliente marca o proprio atendimento")
    void clienteMarcaAtendimento() {
        assertTrue(RegrasAcesso.permitido(Perfil.CLIENTE, "/agendamentos/novo"));
    }

    @Test
    @DisplayName("CT39 - visitante so acessa telas publicas")
    void visitanteSoTelasPublicas() {
        assertTrue(RegrasAcesso.permitido(null, "/login"));
        assertFalse(RegrasAcesso.permitido(null, "/"));
    }

    @Test
    @DisplayName("CT40 - menu nao oferece area sem permissao")
    void menuNaoVazaArea() {
        for (Perfil perfil : Perfil.values()) {
            for (RegrasAcesso.Area area : RegrasAcesso.menuDe(perfil)) {
                assertTrue(RegrasAcesso.permitido(perfil, area.caminho()),
                        "menu de " + perfil + " ofereceu " + area.caminho());
            }
        }
    }

    @Test
    @DisplayName("CT41 - quadro de permissoes acompanha o filtro")
    void matrizAcompanhaFiltro() {
        for (RegrasAcesso.LinhaMatriz linha : RegrasAcesso.matriz()) {
            assertTrue(linha.administrador() || linha.profissional() || linha.cliente(),
                    "area sem nenhum perfil: " + linha.rotulo());
        }
        assertTrue(RegrasAcesso.matriz().stream().anyMatch(
                l -> "Relatorios".equals(l.rotulo()) && l.administrador() && !l.cliente()));
    }
}
