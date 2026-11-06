package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Profissional;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.servico.AgendaService;
import br.edu.cesumar.agendapro.servico.ProfissionalService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas de horarios de atendimento e bloqueios de agenda (RF05 e RF06). */
@Controller
@RequestMapping("/horarios")
public class HorarioController {

    private final AgendaService agenda;
    private final ProfissionalService profissionais;

    public HorarioController(AgendaService agenda, ProfissionalService profissionais) {
        this.agenda = agenda;
        this.profissionais = profissionais;
    }

    @GetMapping
    public String abrir(@RequestParam(required = false) Long profissional, Model model) {
        List<Profissional> ativos = profissionais.listarAtivos();
        model.addAttribute("profissionais", ativos);

        Long escolhido = profissional;
        if (escolhido == null && !ativos.isEmpty()) {
            escolhido = ativos.get(0).getId();
        }
        model.addAttribute("escolhido", escolhido);
        if (escolhido != null) {
            model.addAttribute("faixas", agenda.faixasDe(escolhido));
            model.addAttribute("bloqueios", agenda.bloqueiosDe(escolhido));
        }
        model.addAttribute("hoje", LocalDate.now());
        return "horarios/index";
    }

    @PostMapping("/faixas")
    public String adicionarFaixa(@RequestParam Long profissional,
                                 @RequestParam int diaSemana,
                                 @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime inicio,
                                 @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime fim,
                                 RedirectAttributes redirect) {
        try {
            agenda.adicionarFaixa(profissional, diaSemana, inicio, fim);
            redirect.addFlashAttribute("sucesso", "Faixa de atendimento adicionada.");
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
        }
        return "redirect:/horarios?profissional=" + profissional;
    }

    @PostMapping("/faixas/{id}/remover")
    public String removerFaixa(@PathVariable Long id,
                               @RequestParam Long profissional,
                               RedirectAttributes redirect) {
        agenda.removerFaixa(id);
        redirect.addFlashAttribute("sucesso", "Faixa removida.");
        return "redirect:/horarios?profissional=" + profissional;
    }

    @PostMapping("/bloqueios")
    public String adicionarBloqueio(@RequestParam Long profissional,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                    @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime inicio,
                                    @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime fim,
                                    @RequestParam(defaultValue = "") String motivo,
                                    RedirectAttributes redirect) {
        try {
            agenda.adicionarBloqueio(profissional, data, inicio, fim, motivo);
            redirect.addFlashAttribute("sucesso",
                    "Bloqueio registrado. Os horarios deixam de ser oferecidos (RN02).");
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
        }
        return "redirect:/horarios?profissional=" + profissional;
    }

    @PostMapping("/bloqueios/{id}/remover")
    public String removerBloqueio(@PathVariable Long id,
                                  @RequestParam Long profissional,
                                  RedirectAttributes redirect) {
        agenda.removerBloqueio(id);
        redirect.addFlashAttribute("sucesso", "Bloqueio removido.");
        return "redirect:/horarios?profissional=" + profissional;
    }
}
