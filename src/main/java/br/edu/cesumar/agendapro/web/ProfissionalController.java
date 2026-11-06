package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Profissional;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.repositorio.ServicoRepository;
import br.edu.cesumar.agendapro.servico.ProfissionalService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas de cadastro e consulta de profissionais (RF03). */
@Controller
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService servico;
    private final ServicoRepository servicos;

    public ProfissionalController(ProfissionalService servico, ServicoRepository servicos) {
        this.servico = servico;
        this.servicos = servicos;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("profissionais", servico.listar());
        return "profissionais/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("profissional", new Profissional());
        model.addAttribute("servicos", servicos.findByAtivoTrueOrderByNome());
        model.addAttribute("selecionados", new ArrayList<Long>());
        return "profissionais/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Profissional profissional = servico.porId(id);
        model.addAttribute("profissional", profissional);
        model.addAttribute("servicos", servicos.findByAtivoTrueOrderByNome());
        model.addAttribute("selecionados",
                profissional.getServicos().stream().map(s -> s.getId()).toList());
        return "profissionais/form";
    }

    @PostMapping
    public String salvar(@RequestParam(required = false) Long id,
                         @RequestParam(defaultValue = "") String nome,
                         @RequestParam(defaultValue = "") String telefone,
                         @RequestParam(defaultValue = "") String email,
                         @RequestParam(required = false) List<Long> servicosSelecionados,
                         Model model,
                         RedirectAttributes redirect) {
        List<Long> selecionados = servicosSelecionados == null ? new ArrayList<>() : servicosSelecionados;
        try {
            Profissional salvo = servico.salvar(id, nome, telefone, email, selecionados);
            redirect.addFlashAttribute("sucesso",
                    "Profissional " + salvo.getNome() + " salvo com sucesso.");
            return "redirect:/profissionais";
        } catch (ValidacaoException e) {
            Profissional preenchido = new Profissional();
            preenchido.setId(id);
            preenchido.setNome(nome);
            preenchido.setTelefone(telefone);
            preenchido.setEmail(email);
            model.addAttribute("profissional", preenchido);
            model.addAttribute("servicos", servicos.findByAtivoTrueOrderByNome());
            model.addAttribute("selecionados", selecionados);
            model.addAttribute("erros", Mensagens.porCampo(e));
            return "profissionais/form";
        }
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, RedirectAttributes redirect) {
        servico.inativar(id);
        redirect.addFlashAttribute("sucesso",
                "Profissional inativado. O cadastro foi preservado (RN12).");
        return "redirect:/profissionais";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, RedirectAttributes redirect) {
        servico.reativar(id);
        redirect.addFlashAttribute("sucesso", "Profissional reativado.");
        return "redirect:/profissionais";
    }
}
