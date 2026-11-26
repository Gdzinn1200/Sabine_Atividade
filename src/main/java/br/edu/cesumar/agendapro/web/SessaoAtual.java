package br.edu.cesumar.agendapro.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Leitura e gravacao do usuario na sessao HTTP. */
public final class SessaoAtual {

    private SessaoAtual() {
    }

    public static UsuarioSessao de(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        if (sessao == null) {
            return null;
        }
        Object valor = sessao.getAttribute(UsuarioSessao.CHAVE);
        return valor instanceof UsuarioSessao u ? u : null;
    }

    public static void gravar(HttpServletRequest request, UsuarioSessao usuario) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        request.getSession(true).setAttribute(UsuarioSessao.CHAVE, usuario);
    }

    public static void encerrar(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
    }
}
