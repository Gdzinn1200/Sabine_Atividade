package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Usuario;
import br.edu.cesumar.agendapro.dominio.regras.Perfil;
import br.edu.cesumar.agendapro.dominio.regras.RegrasAcesso;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.servico.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Contas de acesso e quadro de permissoes (RF17 e RNF02). */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService servico;

    public UsuarioController(UsuarioService servico) {
        this.servico = servico;
    }

    @GetMapping
    public String listar(Model model) {
        popular(model);
        return "usuarios/lista";
    }

    @PostMapping
    public String criar(@RequestParam(defaultValue = "") String nome,
                        @RequestParam(defaultValue = "") String email,
                        @RequestParam(defaultValue = "") String senha,
                        @RequestParam(defaultValue = "") String confirmacao,
                        @RequestParam(required = false) Perfil perfil,
                        Model model,
                        RedirectAttributes redirect) {
        try {
            Usuario criado = servico.criar(nome, email, senha, confirmacao, perfil);
            redirect.addFlashAttribute("sucesso", "Conta de " + criado.getNome()
                    + " criada como " + criado.getPerfil().getRotulo()
                    + ". A senha foi gravada apenas como hash.");
            return "redirect:/usuarios";
        } catch (ValidacaoException e) {
            popular(model);
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            model.addAttribute("erro", Mensagens.resumo(e));
            return "usuarios/lista";
        }
    }

    /** Liga e desliga a conta no mesmo botao, conforme a situacao atual. */
    @PostMapping("/{id}/situacao")
    public String alternarSituacao(@PathVariable Long id, RedirectAttributes redirect) {
        Usuario usuario = servico.porId(id);
        if (usuario.isAtivo()) {
            servico.inativar(id);
            redirect.addFlashAttribute("sucesso",
                    "Conta de " + usuario.getNome() + " desativada. O acesso foi bloqueado.");
        } else {
            servico.reativar(id);
            redirect.addFlashAttribute("sucesso",
                    "Conta de " + usuario.getNome() + " reativada.");
        }
        return "redirect:/usuarios";
    }

    private void popular(Model model) {
        model.addAttribute("usuarios", servico.listar());
        model.addAttribute("perfis", Perfil.values());
        model.addAttribute("matriz", RegrasAcesso.matriz());
    }
}
