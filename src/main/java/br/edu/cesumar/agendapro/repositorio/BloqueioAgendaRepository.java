package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.BloqueioAgenda;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {

    List<BloqueioAgenda> findByProfissionalIdOrderByDataHoraInicio(Long idProfissional);
}
