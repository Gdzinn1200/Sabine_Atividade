package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import br.edu.cesumar.agendapro.dominio.Cliente;
import br.edu.cesumar.agendapro.dominio.Profissional;
import br.edu.cesumar.agendapro.dominio.Servico;
import br.edu.cesumar.agendapro.dominio.regras.Perfil;
import br.edu.cesumar.agendapro.dominio.regras.RegrasAgenda;
import br.edu.cesumar.agendapro.dominio.regras.RegrasSituacao;
import br.edu.cesumar.agendapro.dominio.regras.SituacaoAgendamento;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.repositorio.ClienteRepository;
import br.edu.cesumar.agendapro.repositorio.ServicoRepository;
import br.edu.cesumar.agendapro.servico.AgendamentoService;
import br.edu.cesumar.agendapro.servico.NotificacaoService;
import br.edu.cesumar.agendapro.servico.ProfissionalService;
import jakarta.servlet.http.HttpServletRequest;
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

/**
 * Fluxo do agendamento (RF07 a RF13).
 *
 * RNF02: alem do filtro de acesso por area, cada consulta e limitada ao que o
 * perfil pode ver. O profissional ve a propria agenda; o cliente ve apenas os
 * proprios atendimentos.
 */
@Controller
public class AgendamentoController {

    private final AgendamentoService agendamentos;
    private final ProfissionalService profissionais;
    private final ServicoRepository servicos;
    private final ClienteRepository clientes;
    private final NotificacaoService notificacoes;

    public AgendamentoController(AgendamentoService agendamentos,
                                 ProfissionalService profissionais,
                                 ServicoRepository servicos,
                                 ClienteRepository clientes,
                                 NotificacaoService notificacoes) {
        this.agendamentos = agendamentos;
        this.profissionais = profissionais;
        this.servicos = servicos;
        this.clientes = clientes;
        this.notificacoes = notificacoes;
    }

    // ------------------------------------------------------------ RF12
    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                         @RequestParam(required = false) Long profissional,
                         @RequestParam(required = false) SituacaoAgendamento situacao,
                         Model model,
                         HttpServletRequest request) {

        UsuarioSessao usuario = SessaoAtual.de(request);
        LocalDate dia = data == null ? LocalDate.now() : data;

        // RNF02: o profissional so enxerga a propria agenda.
        Long filtroProfissional = profissional;
        if (usuario != null && usuario.isProfissional()) {
            filtroProfissional = usuario.idProfissional();
        }

        model.addAttribute("data", dia);
        model.addAttribute("dataFormatada",
                dia.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("profissional", filtroProfissional);
        model.addAttribute("travarProfissional", usuario != null && usuario.isProfissional());
        model.addAttribute("situacao", situacao);
        model.addAttribute("profissionais", profissionais.listarAtivos());
        model.addAttribute("situacoes", SituacaoAgendamento.values());
        model.addAttribute("agendamentos", agendamentos.agendaDoDia(dia, filtroProfissional, situacao));
        return "agenda/index";
    }

    // ------------------------------------------------------------ RF07 e RF08
    @GetMapping("/agendamentos/novo")
    public String novo(@RequestParam(required = false) Long cliente,
                       @RequestParam(required = false) Long servico,
                       @RequestParam(required = false) Long profissional,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                       Model model,
                       HttpServletRequest request) {

        UsuarioSessao usuario = SessaoAtual.de(request);
        LocalDate dia = data == null ? LocalDate.now().plusDays(1) : data;

        // RNF02: o cliente so agenda para si mesmo.
        Long idCliente = cliente;
        if (usuario != null && usuario.isCliente()) {
            idCliente = usuario.idCliente();
        }

        List<Servico> ativos = servicos.findByAtivoTrueOrderByNome();
        Long idServico = servico;
        if (idServico == null && !ativos.isEmpty()) {
            idServico = ativos.get(0).getId();
        }

        final Long servicoEscolhido = idServico;
        List<Profissional> habilitados = servicoEscolhido == null
                ? List.of() : profissionais.habilitadosPara(servicoEscolhido);

        Long idProfissional = profissional;
        if ((idProfissional == null
                || habilitados.stream().noneMatch(p -> p.getId().equals(profissional)))
                && !habilitados.isEmpty()) {
            idProfissional = habilitados.get(0).getId();
        }

        model.addAttribute("clientes", clientes.findByAtivoTrueOrderByNome());
        model.addAttribute("servicos", ativos);
        model.addAttribute("habilitados", habilitados);
        model.addAttribute("idCliente", idCliente);
        model.addAttribute("idServico", idServico);
        model.addAttribute("idProfissional", idProfissional);
        model.addAttribute("data", dia);
        model.addAttribute("travarCliente", usuario != null && usuario.isCliente());

        if (idCliente != null) {
            clientes.findById(idCliente).ifPresent(c -> model.addAttribute("clienteEscolhido", c));
        }
        if (idServico != null && idProfissional != null) {
            List<RegrasAgenda.Horario> horarios =
                    agendamentos.horariosDisponiveis(idProfissional, idServico, dia, null);
            model.addAttribute("horarios", horarios);
            model.addAttribute("livres", RegrasAgenda.quantidadeLivre(horarios));
        }
        return "agendamentos/novo";
    }

    @PostMapping("/agendamentos")
    public String agendar(@RequestParam(required = false) Long cliente,
                          @RequestParam Long servico,
                          @RequestParam Long profissional,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                          @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
                          @RequestParam(defaultValue = "") String observacao,
                          RedirectAttributes redirect,
                          HttpServletRequest request) {

        UsuarioSessao usuario = SessaoAtual.de(request);
        Long idCliente = usuario != null && usuario.isCliente() ? usuario.idCliente() : cliente;

        if (idCliente == null) {
            redirect.addFlashAttribute("erro", "Selecione o cliente do atendimento.");
            return "redirect:/agendamentos/novo?servico=" + servico
                    + "&profissional=" + profissional + "&data=" + data;
        }
        try {
            Agendamento salvo = agendamentos.agendar(idCliente, profissional, servico, data, hora, observacao);
            redirect.addFlashAttribute("sucesso",
                    "Agendamento gravado para " + salvo.getCliente().getNome() + " em "
                            + salvo.getDataFormatada() + " as " + salvo.getHoraInicioFormatada()
                            + ". Confirmacao registrada para o cliente.");
            return "redirect:/agendamentos/" + salvo.getId();
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
            return "redirect:/agendamentos/novo?cliente=" + idCliente + "&servico=" + servico
                    + "&profissional=" + profissional + "&data=" + data;
        }
    }

    // ------------------------------------------------------------ RF09 a RF11
    @GetMapping("/agendamentos/{id}")
    public String detalhe(@PathVariable Long id, Model model, HttpServletRequest request) {
        Agendamento agendamento = agendamentos.porId(id);
        if (!podeVer(agendamento, SessaoAtual.de(request))) {
            return "redirect:/sem-acesso";
        }
        model.addAttribute("agendamento", agendamento);
        model.addAttribute("historico", agendamentos.historicoDe(id));
        model.addAttribute("avisos", notificacoes.doAgendamento(id));
        model.addAttribute("proximas", RegrasSituacao.proximas(agendamento.getSituacao()));
        model.addAttribute("antecedencia", RegrasSituacao.HORAS_ANTECEDENCIA);
        return "agendamentos/detalhe";
    }

    @PostMapping("/agendamentos/{id}/situacao")
    public String mudarSituacao(@PathVariable Long id,
                                @RequestParam SituacaoAgendamento nova,
                                @RequestParam(defaultValue = "") String motivo,
                                RedirectAttributes redirect,
                                HttpServletRequest request) {
        UsuarioSessao usuario = SessaoAtual.de(request);
        if (usuario != null && usuario.isCliente()) {
            return "redirect:/sem-acesso";   // RNF02: o cliente nao conduz o atendimento
        }
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
                           RedirectAttributes redirect,
                           HttpServletRequest request) {
        UsuarioSessao usuario = SessaoAtual.de(request);
        Agendamento agendamento = agendamentos.porId(id);
        if (!podeVer(agendamento, usuario)) {
            return "redirect:/sem-acesso";
        }
        // RN05: quem solicita vem da sessao, nao de um campo do formulario.
        Perfil solicitante = usuario == null ? Perfil.ADMINISTRADOR : usuario.perfil();
        try {
            agendamentos.cancelar(id, motivo, solicitante);
            redirect.addFlashAttribute("sucesso",
                    "Agendamento cancelado. O horario voltou a ficar disponivel e o cliente foi avisado.");
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
        }
        return "redirect:/agendamentos/" + id;
    }

    @GetMapping("/agendamentos/{id}/remarcar")
    public String telaRemarcar(@PathVariable Long id,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                               Model model,
                               HttpServletRequest request) {
        Agendamento agendamento = agendamentos.porId(id);
        if (!podeVer(agendamento, SessaoAtual.de(request))) {
            return "redirect:/sem-acesso";
        }
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
                           RedirectAttributes redirect,
                           HttpServletRequest request) {
        Agendamento agendamento = agendamentos.porId(id);
        if (!podeVer(agendamento, SessaoAtual.de(request))) {
            return "redirect:/sem-acesso";
        }
        try {
            agendamentos.remarcar(id, agendamento.getProfissional().getId(), data, hora);
            redirect.addFlashAttribute("sucesso", "Agendamento remarcado. O cliente foi avisado.");
            return "redirect:/agendamentos/" + id;
        } catch (ValidacaoException e) {
            redirect.addFlashAttribute("erro", Mensagens.resumo(e));
            return "redirect:/agendamentos/" + id + "/remarcar?data=" + data;
        }
    }

    // ------------------------------------------------------------ RF13
    @GetMapping("/clientes/{id}/historico")
    public String historicoDoCliente(@PathVariable Long id, Model model, HttpServletRequest request) {
        UsuarioSessao usuario = SessaoAtual.de(request);
        if (usuario != null && usuario.isCliente() && !id.equals(usuario.idCliente())) {
            return "redirect:/sem-acesso";
        }
        return montarHistorico(id, model);
    }

    /** Atalho do proprio cliente para o seu historico. */
    @GetMapping("/meu-historico")
    public String meuHistorico(Model model, HttpServletRequest request) {
        UsuarioSessao usuario = SessaoAtual.de(request);
        if (usuario == null || usuario.idCliente() == null) {
            return "redirect:/sem-acesso";
        }
        return montarHistorico(usuario.idCliente(), model);
    }

    private String montarHistorico(Long idCliente, Model model) {
        Cliente cliente = clientes.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado: " + idCliente));
        model.addAttribute("cliente", cliente);
        model.addAttribute("agendamentos", agendamentos.historicoDoCliente(idCliente));
        return "clientes/historico";
    }

    /** RNF02: o cliente so abre os proprios atendimentos; o profissional, os seus. */
    private boolean podeVer(Agendamento agendamento, UsuarioSessao usuario) {
        if (usuario == null || usuario.isAdministrador()) {
            return true;
        }
        if (usuario.isCliente()) {
            return agendamento.getCliente() != null
                    && agendamento.getCliente().getId().equals(usuario.idCliente());
        }
        if (usuario.isProfissional()) {
            return agendamento.getProfissional() != null
                    && agendamento.getProfissional().getId().equals(usuario.idProfissional());
        }
        return false;
    }
}
