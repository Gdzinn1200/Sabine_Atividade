package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.regras.RegrasNotificacao;
import br.edu.cesumar.agendapro.servico.NotificacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Registro das mensagens enviadas e disparo da rotina de lembretes (RF14, RF15). */
@Controller
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService servico;

    public NotificacaoController(NotificacaoService servico) {
        this.servico = servico;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("notificacoes", servico.ultimas());
        model.addAttribute("antecedencia", RegrasNotificacao.HORAS_ANTECEDENCIA_LEMBRETE);
        return "notificacoes/index";
    }

    @PostMapping("/lembretes")
    public String executarLembretes(RedirectAttributes redirect) {
        int enviados = servico.enviarLembretes();
        if (enviados > 0) {
            redirect.addFlashAttribute("sucesso",
                    enviados + " lembrete(s) enviado(s) nesta execucao.");
        } else {
            redirect.addFlashAttribute("aviso",
                    "Nenhum lembrete a enviar: nao ha atendimento ativo nas proximas "
                            + RegrasNotificacao.HORAS_ANTECEDENCIA_LEMBRETE
                            + " horas sem lembrete registrado (RN11).");
        }
        return "redirect:/notificacoes";
    }
}
