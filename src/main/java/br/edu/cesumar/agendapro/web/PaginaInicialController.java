package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import br.edu.cesumar.agendapro.dominio.regras.SituacaoAgendamento;
import br.edu.cesumar.agendapro.repositorio.ClienteRepository;
import br.edu.cesumar.agendapro.repositorio.ProfissionalRepository;
import br.edu.cesumar.agendapro.repositorio.ServicoRepository;
import br.edu.cesumar.agendapro.servico.AgendamentoService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Painel inicial. O conteudo muda conforme o perfil autenticado (RNF02). */
@Controller
public class PaginaInicialController {

    private final ClienteRepository clientes;
    private final ServicoRepository servicos;
    private final ProfissionalRepository profissionais;
    private final AgendamentoService agendamentos;

    public PaginaInicialController(ClienteRepository clientes,
                                   ServicoRepository servicos,
                                   ProfissionalRepository profissionais,
                                   AgendamentoService agendamentos) {
        this.clientes = clientes;
        this.servicos = servicos;
        this.profissionais = profissionais;
        this.agendamentos = agendamentos;
    }

    @GetMapping("/")
    public String inicio(Model model, HttpServletRequest request) {
        UsuarioSessao usuario = SessaoAtual.de(request);
        LocalDate hoje = LocalDate.now();
        model.addAttribute("hoje", hoje);
        model.addAttribute("hojeFormatado",
                hoje.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (usuario != null && usuario.isCliente()) {
            List<Agendamento> meus = agendamentos.historicoDoCliente(usuario.idCliente());
            LocalDateTime agora = LocalDateTime.now();
            model.addAttribute("proximos", meus.stream()
                    .filter(a -> a.getDataHoraInicio().isAfter(agora))
                    .filter(a -> a.getSituacao().isAtiva())
                    .sorted((x, y) -> x.getDataHoraInicio().compareTo(y.getDataHoraInicio()))
                    .toList());
            model.addAttribute("anteriores", meus.stream()
                    .filter(a -> a.getSituacao().isFinal())
                    .limit(5)
                    .toList());
            clientes.findById(usuario.idCliente())
                    .ifPresent(c -> model.addAttribute("cliente", c));
            return "painel/cliente";
        }

        Long filtroProfissional = usuario != null && usuario.isProfissional()
                ? usuario.idProfissional() : null;
        List<Agendamento> doDia = agendamentos.agendaDoDia(hoje, filtroProfissional, null);

        BigDecimal previsto = BigDecimal.ZERO;
        for (Agendamento a : doDia) {
            if (a.getSituacao().isAtiva() || a.getSituacao() == SituacaoAgendamento.CONCLUIDO) {
                previsto = previsto.add(a.getValor());
            }
        }

        model.addAttribute("agendamentosHoje", doDia);
        model.addAttribute("previsto", previsto);
        model.addAttribute("aguardando", doDia.stream()
                .filter(a -> a.getSituacao() == SituacaoAgendamento.AGENDADO).count());
        model.addAttribute("cancelados", doDia.stream()
                .filter(a -> a.getSituacao() == SituacaoAgendamento.CANCELADO).count());
        model.addAttribute("totalClientes", clientes.findByAtivoTrueOrderByNome().size());
        model.addAttribute("totalServicos", servicos.findByAtivoTrueOrderByNome().size());
        model.addAttribute("totalProfissionais", profissionais.findByAtivoTrueOrderByNome().size());
        model.addAttribute("bloqueados", clientes.findAll().stream()
                .filter(c -> c.isBloqueado() && c.isAtivo()).toList());
        return "painel/equipe";
    }
}
