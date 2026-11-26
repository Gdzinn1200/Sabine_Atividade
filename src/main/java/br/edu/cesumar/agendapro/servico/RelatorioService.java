package br.edu.cesumar.agendapro.servico;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import br.edu.cesumar.agendapro.dominio.regras.RegrasRelatorio;
import br.edu.cesumar.agendapro.repositorio.AgendamentoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Relatorios de atendimentos por periodo (RF16). Acesso restrito pela RN13. */
@Service
public class RelatorioService {

    private final AgendamentoRepository agendamentos;

    public RelatorioService(AgendamentoRepository agendamentos) {
        this.agendamentos = agendamentos;
    }

    /** Carrega os atendimentos do periodo no formato que as regras esperam. */
    public List<RegrasRelatorio.Atendimento> atendimentos(LocalDate de, LocalDate ate,
                                                          Long idProfissional, Long idServico) {
        List<RegrasRelatorio.Atendimento> lista = new ArrayList<>();
        for (Agendamento a : agendamentos.findByDataHoraInicioBetweenOrderByDataHoraInicio(
                de.atStartOfDay(), ate.atTime(java.time.LocalTime.MAX))) {

            if (idProfissional != null && !a.getProfissional().getId().equals(idProfissional)) {
                continue;
            }
            if (idServico != null && !a.getServico().getId().equals(idServico)) {
                continue;
            }
            lista.add(new RegrasRelatorio.Atendimento(
                    a.getDataHoraInicio().toLocalDate(),
                    a.getProfissional().getNome(),
                    a.getServico().getNome(),
                    a.getSituacao(),
                    a.getValor()));
        }
        return lista;
    }
}
