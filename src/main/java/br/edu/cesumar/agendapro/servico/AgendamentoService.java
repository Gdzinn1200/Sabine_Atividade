package br.edu.cesumar.agendapro.servico;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import br.edu.cesumar.agendapro.dominio.Cliente;
import br.edu.cesumar.agendapro.dominio.HistoricoSituacao;
import br.edu.cesumar.agendapro.dominio.Profissional;
import br.edu.cesumar.agendapro.dominio.Servico;
import br.edu.cesumar.agendapro.dominio.regras.RegrasAgenda;
import br.edu.cesumar.agendapro.dominio.regras.RegrasSituacao;
import br.edu.cesumar.agendapro.dominio.regras.SituacaoAgendamento;
import br.edu.cesumar.agendapro.repositorio.AgendamentoRepository;
import br.edu.cesumar.agendapro.repositorio.ClienteRepository;
import br.edu.cesumar.agendapro.repositorio.HistoricoSituacaoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fluxo do agendamento (RF07 a RF12). As decisoes ficam em
 * {@link RegrasAgenda} e {@link RegrasSituacao}; aqui apenas carregamos os dados,
 * chamamos as regras e gravamos o resultado.
 */
@Service
public class AgendamentoService {

    private final AgendamentoRepository repositorio;
    private final HistoricoSituacaoRepository historico;
    private final ClienteRepository clientes;
    private final ClienteService clienteService;
    private final ProfissionalService profissionais;
    private final ServicoService servicos;
    private final AgendaService agenda;

    public AgendamentoService(AgendamentoRepository repositorio,
                              HistoricoSituacaoRepository historico,
                              ClienteRepository clientes,
                              ClienteService clienteService,
                              ProfissionalService profissionais,
                              ServicoService servicos,
                              AgendaService agenda) {
        this.repositorio = repositorio;
        this.historico = historico;
        this.clientes = clientes;
        this.clienteService = clienteService;
        this.profissionais = profissionais;
        this.servicos = servicos;
        this.agenda = agenda;
    }

    public Agendamento porId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento nao encontrado: " + id));
    }

    /** RF12: agenda de um dia, opcionalmente filtrada por profissional e situacao. */
    public List<Agendamento> agendaDoDia(LocalDate data, Long idProfissional, SituacaoAgendamento situacao) {
        List<Agendamento> doDia = repositorio.findByDataHoraInicioBetweenOrderByDataHoraInicio(
                data.atStartOfDay(), data.atTime(LocalTime.MAX));
        return doDia.stream()
                .filter(a -> idProfissional == null || a.getProfissional().getId().equals(idProfissional))
                .filter(a -> situacao == null || a.getSituacao() == situacao)
                .toList();
    }

    /** RF13: historico de atendimentos de um cliente. */
    public List<Agendamento> historicoDoCliente(Long idCliente) {
        return repositorio.findByClienteIdOrderByDataHoraInicioDesc(idCliente);
    }

    public List<HistoricoSituacao> historicoDe(Long idAgendamento) {
        return historico.findByAgendamentoIdOrderByDataHora(idAgendamento);
    }

    /** RF07: horarios livres do profissional para o servico na data informada. */
    public List<RegrasAgenda.Horario> horariosDisponiveis(Long idProfissional, Long idServico,
                                                          LocalDate data, Long idIgnorar) {
        Servico servico = servicos.porId(idServico);
        return RegrasAgenda.horariosDisponiveis(idProfissional, data, servico.getDuracaoMinutos(),
                agenda.estadoDaAgenda(idProfissional, data), LocalDateTime.now(), idIgnorar);
    }

    /** RF08: grava o agendamento depois de aplicar todas as regras. */
    @Transactional
    public Agendamento agendar(Long idCliente, Long idProfissional, Long idServico,
                               LocalDate data, LocalTime hora, String observacao) {
        Cliente cliente = clienteService.porId(idCliente);
        Profissional profissional = profissionais.porId(idProfissional);
        Servico servico = servicos.porId(idServico);

        RegrasAgenda.Pedido pedido = new RegrasAgenda.Pedido(null, cliente.getId(), cliente.isBloqueado(),
                profissional.getId(), profissional.isAtivo(), profissional.executa(idServico),
                data, hora, servico.getDuracaoMinutos());

        RegrasAgenda.validarAgendamentoOuFalhar(pedido, agenda.estadoDaAgenda(idProfissional, data),
                LocalDateTime.now());

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServico(servico);
        agendamento.setDataHoraInicio(LocalDateTime.of(data, hora));
        agendamento.setDataHoraFim(LocalDateTime.of(data,
                RegrasAgenda.termino(hora, servico.getDuracaoMinutos())));
        agendamento.setSituacao(SituacaoAgendamento.AGENDADO);
        agendamento.setValor(servico.getPreco());
        agendamento.setObservacao(observacao == null || observacao.isBlank() ? null : observacao.trim());
        agendamento.setCriadoEm(LocalDateTime.now());

        Agendamento salvo = repositorio.save(agendamento);
        registrar(salvo, null, SituacaoAgendamento.AGENDADO, "Agendamento criado");
        return salvo;
    }

    /** RF09: remarca o atendimento mantendo o registro da alteracao. */
    @Transactional
    public Agendamento remarcar(Long idAgendamento, Long idProfissional, LocalDate data, LocalTime hora) {
        Agendamento agendamento = porId(idAgendamento);
        RegrasSituacao.validarCancelamentoOuFalhar(agendamento.getSituacao(),
                agendamento.getDataHoraInicio(), LocalDateTime.now(),
                RegrasSituacao.Perfil.ADMINISTRADOR, "remarcacao");

        Profissional profissional = profissionais.porId(idProfissional);
        Servico servico = agendamento.getServico();

        RegrasAgenda.Pedido pedido = new RegrasAgenda.Pedido(agendamento.getId(),
                agendamento.getCliente().getId(), agendamento.getCliente().isBloqueado(),
                profissional.getId(), profissional.isAtivo(), profissional.executa(servico.getId()),
                data, hora, servico.getDuracaoMinutos());

        RegrasAgenda.validarAgendamentoOuFalhar(pedido, agenda.estadoDaAgenda(idProfissional, data),
                LocalDateTime.now());

        String anterior = agendamento.getDataHoraInicio().toString();
        agendamento.setProfissional(profissional);
        agendamento.setDataHoraInicio(LocalDateTime.of(data, hora));
        agendamento.setDataHoraFim(LocalDateTime.of(data,
                RegrasAgenda.termino(hora, servico.getDuracaoMinutos())));

        Agendamento salvo = repositorio.save(agendamento);
        registrar(salvo, salvo.getSituacao(), salvo.getSituacao(),
                "Remarcado de " + anterior + " para " + salvo.getDataHoraInicio());
        return salvo;
    }

    /** RF11 com RN09: avanca a situacao do atendimento. */
    @Transactional
    public Agendamento mudarSituacao(Long idAgendamento, SituacaoAgendamento nova, String motivo) {
        Agendamento agendamento = porId(idAgendamento);
        SituacaoAgendamento anterior = agendamento.getSituacao();
        RegrasSituacao.validarTransicaoOuFalhar(anterior, nova);

        agendamento.setSituacao(nova);
        Agendamento salvo = repositorio.save(agendamento);
        registrar(salvo, anterior, nova, motivo);

        if (nova == SituacaoAgendamento.NAO_COMPARECEU) {
            aplicarBloqueioPorFaltas(salvo.getCliente());
        }
        return salvo;
    }

    /** RF10 com RN05 e RN09: cancela o atendimento e libera o horario. */
    @Transactional
    public Agendamento cancelar(Long idAgendamento, String motivo, RegrasSituacao.Perfil solicitante) {
        Agendamento agendamento = porId(idAgendamento);
        RegrasSituacao.validarCancelamentoOuFalhar(agendamento.getSituacao(),
                agendamento.getDataHoraInicio(), LocalDateTime.now(), solicitante, motivo);

        SituacaoAgendamento anterior = agendamento.getSituacao();
        agendamento.setSituacao(SituacaoAgendamento.CANCELADO);
        Agendamento salvo = repositorio.save(agendamento);
        registrar(salvo, anterior, SituacaoAgendamento.CANCELADO, motivo);
        return salvo;
    }

    /** RN10: bloqueia o cliente que acumulou faltas na janela de 90 dias. */
    private void aplicarBloqueioPorFaltas(Cliente cliente) {
        List<RegrasAgenda.Compromisso> compromissos = repositorio.findAll().stream()
                .map(AgendaService::paraCompromisso)
                .toList();
        if (!cliente.isBloqueado()
                && RegrasAgenda.deveBloquearPorFaltas(cliente.getId(), compromissos, LocalDate.now())) {
            cliente.setBloqueado(true);
            clientes.save(cliente);
        }
    }

    /** RNF10: registra a mudanca com data, hora e usuario responsavel. */
    private void registrar(Agendamento agendamento, SituacaoAgendamento de,
                           SituacaoAgendamento para, String motivo) {
        HistoricoSituacao registro = new HistoricoSituacao();
        registro.setAgendamento(agendamento);
        registro.setSituacaoAnterior(de);
        registro.setSituacaoNova(para);
        registro.setDataHora(LocalDateTime.now());
        registro.setMotivo(motivo == null || motivo.isBlank() ? null : motivo.trim());
        historico.save(registro);
    }
}
