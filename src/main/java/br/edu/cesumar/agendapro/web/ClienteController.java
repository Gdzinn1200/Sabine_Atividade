package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Cliente;
import br.edu.cesumar.agendapro.dominio.regras.ErroValidacao;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.servico.ClienteService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas de cadastro e consulta de clientes (RF01). */
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService servico;

    public ClienteController(ClienteService servico) {
        this.servico = servico;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String termo,
                         @RequestParam(defaultValue = "true") boolean somenteAtivos,
                         Model model) {
        model.addAttribute("clientes", servico.listar(termo, somenteAtivos));
        model.addAttribute("termo", termo);
        model.addAttribute("somenteAtivos", somenteAtivos);
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", servico.porId(id));
        return "clientes/form";
    }

    @PostMapping
    public String salvar(@ModelAttribute Cliente cliente,
                         Model model,
                         RedirectAttributes redirect) {
        try {
            Cliente salvo = servico.salvar(cliente);
            redirect.addFlashAttribute("sucesso",
                    "Cliente " + salvo.getNome() + " salvo com sucesso.");
            return "redirect:/clientes";
        } catch (ValidacaoException e) {
            model.addAttribute("cliente", cliente);
            model.addAttribute("erros", porCampo(e));
            return "clientes/form";
        }
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, RedirectAttributes redirect) {
        servico.inativar(id);
        redirect.addFlashAttribute("sucesso",
                "Cliente inativado. O historico de atendimentos foi preservado (RN12).");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, RedirectAttributes redirect) {
        servico.reativar(id);
        redirect.addFlashAttribute("sucesso", "Cliente reativado.");
        return "redirect:/clientes";
    }

    private Map<String, String> porCampo(ValidacaoException e) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (ErroValidacao erro : e.getErros()) {
            mapa.putIfAbsent(erro.campo(), erro.mensagemCompleta());
        }
        return mapa;
    }
}
