package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import br.edu.cesumar.agendapro.dominio.regras.SituacaoAgendamento;
import br.edu.cesumar.agendapro.repositorio.ClienteRepository;
import br.edu.cesumar.agendapro.repositorio.ProfissionalRepository;
import br.edu.cesumar.agendapro.repositorio.ServicoRepository;
import br.edu.cesumar.agendapro.servico.AgendamentoService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String inicio(Model model) {
        LocalDate hoje = LocalDate.now();
        List<Agendamento> doDia = agendamentos.agendaDoDia(hoje, null, null);

        model.addAttribute("hoje", hoje);
        model.addAttribute("totalClientes", clientes.findByAtivoTrueOrderByNome().size());
        model.addAttribute("totalServicos", servicos.findByAtivoTrueOrderByNome().size());
        model.addAttribute("totalProfissionais", profissionais.findByAtivoTrueOrderByNome().size());
        model.addAttribute("agendamentosHoje", doDia);
        model.addAttribute("aguardando", doDia.stream()
                .filter(a -> a.getSituacao() == SituacaoAgendamento.AGENDADO).count());
        return "inicio";
    }
}
