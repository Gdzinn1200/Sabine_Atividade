package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.regras.RegrasAcesso;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * RNF02: toda requisicao passa por aqui. Quem nao esta autenticado vai para o
 * login; quem esta mas nao tem o perfil da area vai para a tela de acesso
 * negado. A decisao e de {@link RegrasAcesso}, a mesma que monta o menu, entao
 * nao existe tela alcancavel pela URL que o menu esconde.
 */
@Component
public class SessaoFiltro implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String base = request.getContextPath();
        String caminho = request.getRequestURI();
        if (base != null && !base.isEmpty() && caminho.startsWith(base)) {
            caminho = caminho.substring(base.length());
        }
        if (caminho.isEmpty()) {
            caminho = "/";
        }

        if (RegrasAcesso.publico(caminho)) {
            chain.doFilter(req, res);
            return;
        }

        UsuarioSessao usuario = SessaoAtual.de(request);
        if (usuario == null) {
            response.sendRedirect(base + "/login");
            return;
        }

        if (!RegrasAcesso.permitido(usuario.perfil(), caminho)) {
            response.sendRedirect(base + "/sem-acesso");
            return;
        }

        chain.doFilter(req, res);
    }
}
