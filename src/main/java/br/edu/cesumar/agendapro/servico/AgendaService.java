package br.edu.cesumar.agendapro.servico;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import br.edu.cesumar.agendapro.dominio.BloqueioAgenda;
import br.edu.cesumar.agendapro.dominio.HorarioAtendimento;
import br.edu.cesumar.agendapro.dominio.Profissional;
import br.edu.cesumar.agendapro.dominio.regras.RegrasAgenda;
import br.edu.cesumar.agendapro.dominio.regras.RegrasProfissional;
import br.edu.cesumar.agendapro.repositorio.AgendamentoRepository;
import br.edu.cesumar.agendapro.repositorio.BloqueioAgendaRepository;
import br.edu.cesumar.agendapro.repositorio.HorarioAtendimentoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Disponibilidade do profissional: faixas de atendimento (RF05) e bloqueios (RF06).
 * Tambem monta o estado da agenda que as regras de dominio recebem.
 */
@Service
public class AgendaService {

    private final HorarioAtendimentoRepository horarios;
    private final BloqueioAgendaRepository bloqueios;
    private final AgendamentoRepository agendamentos;
    private final ProfissionalService profissionais;

    public AgendaService(HorarioAtendimentoRepository horarios,
                         BloqueioAgendaRepository bloqueios,
                         AgendamentoRepository agendamentos,
                         ProfissionalService profissionais) {
        this.horarios = horarios;
        this.bloqueios = bloqueios;
        this.agendamentos = agendamentos;
        this.profissionais = profissionais;
    }

    public List<HorarioAtendimento> faixasDe(Long idProfissional) {
        return horarios.findByProfissionalIdOrderByDiaSemanaAscHoraInicioAsc(idProfissional);
    }

    public List<BloqueioAgenda> bloqueiosDe(Long idProfissional) {
        return bloqueios.findByProfissionalIdOrderByDataHoraInicio(idProfissional);
    }

    /** RF05 com RN02: grava uma faixa de atendimento depois de validar. */
    @Transactional
    public void adicionarFaixa(Long idProfissional, int diaSemana, LocalTime inicio, LocalTime fim) {
        List<RegrasProfissional.FaixaExistente> existentes = faixasDe(idProfissional).stream()
                .map(h -> new RegrasProfissional.FaixaExistente(h.getId(), h.getDiaSemana(),
                        h.getHoraInicio(), h.getHoraFim()))
                .toList();
        RegrasProfissional.validarFaixaOuFalhar(diaSemana, inicio, fim, existentes);

        Profissional profissional = profissionais.porId(idProfissional);
        HorarioAtendimento faixa = new HorarioAtendimento();
        faixa.setProfissional(profissional);
        faixa.setDiaSemana(diaSemana);
        faixa.setHoraInicio(inicio);
        faixa.setHoraFim(fim);
        horarios.save(faixa);
    }

    @Transactional
    public void removerFaixa(Long idFaixa) {
        horarios.deleteById(idFaixa);
    }

    /** RF06 com RN02: grava um bloqueio depois de validar o periodo. */
    @Transactional
    public void adicionarBloqueio(Long idProfissional, LocalDate data, LocalTime inicio,
                                  LocalTime fim, String motivo) {
        RegrasProfissional.validarBloqueioOuFalhar(inicio, fim);
        Profissional profissional = profissionais.porId(idProfissional);
        BloqueioAgenda bloqueio = new BloqueioAgenda();
        bloqueio.setProfissional(profissional);
        bloqueio.setDataHoraInicio(LocalDateTime.of(data, inicio));
        bloqueio.setDataHoraFim(LocalDateTime.of(data, fim));
        bloqueio.setMotivo(motivo == null || motivo.isBlank() ? null : motivo.trim());
        bloqueios.save(bloqueio);
    }

    @Transactional
    public void removerBloqueio(Long idBloqueio) {
        bloqueios.deleteById(idBloqueio);
    }

    /**
     * Monta o estado da agenda para as regras de dominio. O volume de dados de um
     * pequeno negocio permite carregar os registros e filtrar em memoria.
     */
    public RegrasAgenda.Agenda estadoDaAgenda(Long idProfissional, LocalDate data) {
        List<RegrasAgenda.Faixa> faixas = new ArrayList<>();
        if (idProfissional != null) {
            for (HorarioAtendimento h : faixasDe(idProfissional)) {
                faixas.add(new RegrasAgenda.Faixa(idProfissional, h.getDiaSemana(),
                        h.getHoraInicio(), h.getHoraFim()));
            }
        }

        List<RegrasAgenda.Bloqueio> listaBloqueios = new ArrayList<>();
        if (idProfissional != null && data != null) {
            for (BloqueioAgenda b : bloqueiosDe(idProfissional)) {
                RegrasAgenda.Bloqueio recorte = recorteNoDia(b, data);
                if (recorte != null) {
                    listaBloqueios.add(recorte);
                }
            }
        }

        List<RegrasAgenda.Compromisso> compromissos = new ArrayList<>();
        for (Agendamento a : agendamentos.findAll()) {
            compromissos.add(paraCompromisso(a));
        }

        return new RegrasAgenda.Agenda(faixas, listaBloqueios, compromissos);
    }

    public static RegrasAgenda.Compromisso paraCompromisso(Agendamento a) {
        return new RegrasAgenda.Compromisso(
                a.getId(),
                a.getProfissional() == null ? null : a.getProfissional().getId(),
                a.getCliente() == null ? null : a.getCliente().getId(),
                a.getDataHoraInicio().toLocalDate(),
                a.getDataHoraInicio().toLocalTime(),
                a.getDataHoraFim().toLocalTime(),
                a.getSituacao());
    }

    /** Recorta um bloqueio de varios dias (ferias, por exemplo) no dia consultado. */
    public static RegrasAgenda.Bloqueio recorteNoDia(BloqueioAgenda b, LocalDate data) {
        LocalDate inicio = b.getDataHoraInicio().toLocalDate();
        LocalDate fim = b.getDataHoraFim().toLocalDate();
        if (data.isBefore(inicio) || data.isAfter(fim)) {
            return null;
        }
        LocalTime horaInicio = data.equals(inicio) ? b.getDataHoraInicio().toLocalTime() : LocalTime.MIN;
        LocalTime horaFim = data.equals(fim) ? b.getDataHoraFim().toLocalTime() : LocalTime.MAX;
        return new RegrasAgenda.Bloqueio(b.getProfissional().getId(), data, horaInicio, horaFim, b.getMotivo());
    }
}
