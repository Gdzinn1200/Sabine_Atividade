package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Servico;
import br.edu.cesumar.agendapro.dominio.regras.ErroValidacao;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.servico.ServicoService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas de cadastro e consulta de servicos (RF04). */
@Controller
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servico;

    public ServicoController(ServicoService servico) {
        this.servico = servico;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicos", servico.listar());
        return "servicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("servico", new Servico());
        return "servicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("servico", servico.porId(id));
        return "servicos/form";
    }

    @PostMapping
    public String salvar(@ModelAttribute("servico") Servico dados,
                         Model model,
                         RedirectAttributes redirect) {
        try {
            Servico salvo = servico.salvar(dados);
            redirect.addFlashAttribute("sucesso",
                    "Servico " + salvo.getNome() + " salvo com sucesso.");
            return "redirect:/servicos";
        } catch (ValidacaoException e) {
            model.addAttribute("servico", dados);
            model.addAttribute("erros", porCampo(e));
            return "servicos/form";
        }
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, RedirectAttributes redirect) {
        servico.inativar(id);
        redirect.addFlashAttribute("sucesso", "Servico inativado (RN12).");
        return "redirect:/servicos";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, RedirectAttributes redirect) {
        servico.reativar(id);
        redirect.addFlashAttribute("sucesso", "Servico reativado.");
        return "redirect:/servicos";
    }

    private Map<String, String> porCampo(ValidacaoException e) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (ErroValidacao erro : e.getErros()) {
            mapa.putIfAbsent(erro.campo(), erro.mensagemCompleta());
        }
        return mapa;
    }
}
