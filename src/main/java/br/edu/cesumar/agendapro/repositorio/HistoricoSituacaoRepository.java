package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.HistoricoSituacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoSituacaoRepository extends JpaRepository<HistoricoSituacao, Long> {

    List<HistoricoSituacao> findByAgendamentoIdOrderByDataHora(Long idAgendamento);
}
