package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import br.edu.cesumar.agendapro.dominio.Profissional;
import br.edu.cesumar.agendapro.dominio.Servico;
import br.edu.cesumar.agendapro.dominio.regras.RegrasAgenda;
import br.edu.cesumar.agendapro.dominio.regras.RegrasSituacao;
import br.edu.cesumar.agendapro.dominio.regras.SituacaoAgendamento;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.repositorio.ClienteRepository;
import br.edu.cesumar.agendapro.repositorio.ServicoRepository;
import br.edu.cesumar.agendapro.servico.AgendamentoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas do fluxo de agendamento: agenda do dia, novo agendamento e detalhe (RF07 a RF12). */
@Controller
public class AgendamentoController {

    private final AgendamentoService agendamentos;
    private final ProfissionalService profissionais;
    private final ServicoRepository servicos;
    private final ClienteRepository clientes;

    public AgendamentoController(AgendamentoService agendamentos,
                                 ProfissionalService profissionais,
                                 ServicoRepository servicos,
                                 ClienteRepository clientes) {
        this.agendamentos = agendamentos;
        this.profissionais = profissionais;
        this.servicos = servicos;
        this.clientes = clientes;
    }

    // ------------------------------------------------------------ RF12
    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                         @RequestParam(required = false) Long profissional,
                         @RequestParam(required = false) SituacaoAgendamento situacao,
                         Model model) {
        LocalDate dia = data == null ? LocalDate.now() : data;
        model.addAttribute("data", dia);
        model.addAttribute("dataFormatada",
                dia.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("profissional", profissional);
        model.addAttribute("situacao", situacao);
        model.addAttribute("profissionais", profissionais.listarAtivos());
        model.addAttribute("situacoes", SituacaoAgendamento.values());
        model.addAttribute("agendamentos", agendamentos.agendaDoDia(dia, profissional, situacao));
        return "agenda/index";
    }

    // ------------------------------------------------------------ RF07 e RF08
    @GetMapping("/agendamentos/novo")
    public String novo(@RequestParam(required = false) Long cliente,
                       @RequestParam(required = false) Long servico,
                       @RequestParam(required = false) Long profissional,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                       Model model) {
        LocalDate dia = data == null ? LocalDate.now().plusDays(1) : data;

        List<Servico> ativos = servicos.findByAtivoTrueOrderByNome();
        Long idServico = servico;
        if (idServico == null && !ativos.isEmpty()) {
            idServico = ativos.get(0).getId();
        }

        List<Profissional> habilitados = idServico == null
                ? List.of() : profissionais.habilitadosPara(idServico);
        Long idProfissional = profissional;
        if ((idProfissional == null || habilitados.stream().noneMatch(p -> p.getId().equals(profissional)))
                && !habilitados.isEmpty()) {
            idProfissional = habilitados.get(0).getId();
        }

        model.addAttribute("clientes", clientes.findByAtivoTrueOrderByNome());
        model.addAttribute("servicos", ativos);
        model.addAttribute("habilitados", habilitados);
        model.addAttribute("idCliente", cliente);
        model.addAttribute("idServico", idServico);
        model.addAttribute("idProfissional", idProfissional);
        model.addAttribute("data", dia);

        if (idServico != null && idProfissional != null) {
            List<RegrasAgenda.Horario> horarios =
                    agendamentos.horariosDisponiveis(idProfissional, idServico, dia, null);
            model.addAttribute("horarios", horarios);
            model.addAttribute("livres", RegrasAgenda.quantidadeLivre(horarios));
            model.addAttribute("servicoEscolhido", servicos.findById(idServico).orElse(null));
        }
        return "agendamentos/novo";
    }

    @PostMapping("/agendamentos")
    public String agendar(@RequestParam Long cliente,
                          @RequestParam Long servico,
                          @RequestParam Long profissional,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                          @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
                          @RequestParam(defaultValue = "") String observacao,
                          RedirectAttributes redirect) {
        try {
            Agendamento salvo = agendamentos.agendar(cliente, profissional, servico, data, hora, observacao);
            redirect.addFlashAttribute("sucesso",
                    "Agendamento gravado para " + salvo.getCliente().getNome() + " em "
                            + salvo.getDataHoraInicio() + ".");
            return "redirect:/agendamentos/" + salvo.getId();
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
            return "redirect:/agendamentos/novo?cliente=" + cliente + "&servico=" + servico
                    + "&profissional=" + profissional + "&data=" + data;
        }
    }

    // ------------------------------------------------------------ RF09 a RF11
    @GetMapping("/agendamentos/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Agendamento agendamento = agendamentos.porId(id);
        model.addAttribute("agendamento", agendamento);
        model.addAttribute("historico", agendamentos.historicoDe(id));
        model.addAttribute("proximas", RegrasSituacao.proximas(agendamento.getSituacao()));
        return "agendamentos/detalhe";
    }

    @PostMapping("/agendamentos/{id}/situacao")
    public String mudarSituacao(@PathVariable Long id,
                                @RequestParam SituacaoAgendamento nova,
                                @RequestParam(defaultValue = "") String motivo,
                                RedirectAttributes redirect) {
        try {
            agendamentos.mudarSituacao(id, nova, motivo);
            redirect.addFlashAttribute("sucesso", "Situacao atualizada para " + nova.getRotulo() + ".");
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
        }
        return "redirect:/agendamentos/" + id;
    }

    @PostMapping("/agendamentos/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           @RequestParam(defaultValue = "") String motivo,
                           @RequestParam(defaultValue = "ADMINISTRADOR") RegrasSituacao.Perfil solicitante,
                           RedirectAttributes redirect) {
        try {
            agendamentos.cancelar(id, motivo, solicitante);
            redirect.addFlashAttribute("sucesso",
                    "Agendamento cancelado. O horario voltou a ficar disponivel.");
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
        }
        return "redirect:/agendamentos/" + id;
    }

    @GetMapping("/agendamentos/{id}/remarcar")
    public String telaRemarcar(@PathVariable Long id,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                               Model model) {
        Agendamento agendamento = agendamentos.porId(id);
        LocalDate dia = data == null ? agendamento.getDataHoraInicio().toLocalDate() : data;

        List<RegrasAgenda.Horario> horarios = agendamentos.horariosDisponiveis(
                agendamento.getProfissional().getId(), agendamento.getServico().getId(), dia, id);

        model.addAttribute("agendamento", agendamento);
        model.addAttribute("data", dia);
        model.addAttribute("horarios", horarios);
        model.addAttribute("livres", RegrasAgenda.quantidadeLivre(horarios));
        return "agendamentos/remarcar";
    }

    @PostMapping("/agendamentos/{id}/remarcar")
    public String remarcar(@PathVariable Long id,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                           @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
                           RedirectAttributes redirect) {
        Agendamento agendamento = agendamentos.porId(id);
        try {
            agendamentos.remarcar(id, agendamento.getProfissional().getId(), data, hora);
            redirect.addFlashAttribute("sucesso", "Agendamento remarcado para " + data + " as " + hora + ".");
            return "redirect:/agendamentos/" + id;
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
            return "redirect:/agendamentos/" + id + "/remarcar?data=" + data;
        }
    }

    // ------------------------------------------------------------ RF13
    @GetMapping("/clientes/{id}/historico")
    public String historicoDoCliente(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clientes.findById(id).orElseThrow());
        model.addAttribute("agendamentos", agendamentos.historicoDoCliente(id));
        return "clientes/historico";
    }
}
