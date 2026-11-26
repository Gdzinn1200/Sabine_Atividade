package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.regras.RegrasAcesso;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Entrega a toda tela o usuario autenticado e o menu do perfil dele, para que
 * o cabecalho seja montado uma vez so e sempre a partir das regras de acesso.
 */
@ControllerAdvice
public class ContextoGlobal {

    @ModelAttribute("usuario")
    public UsuarioSessao usuario(HttpServletRequest request) {
        return SessaoAtual.de(request);
    }

    @ModelAttribute("menu")
    public List<RegrasAcesso.Area> menu(HttpServletRequest request) {
        UsuarioSessao usuario = SessaoAtual.de(request);
        return usuario == null ? List.of() : RegrasAcesso.menuDe(usuario.perfil());
    }

    @ModelAttribute("caminhoAtual")
    public String caminhoAtual(HttpServletRequest request) {
        String base = request.getContextPath();
        String caminho = request.getRequestURI();
        if (base != null && !base.isEmpty() && caminho.startsWith(base)) {
            caminho = caminho.substring(base.length());
        }
        return caminho.isEmpty() ? "/" : caminho;
    }
}
