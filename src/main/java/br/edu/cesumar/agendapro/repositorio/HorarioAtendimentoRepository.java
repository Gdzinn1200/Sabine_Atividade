package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.HorarioAtendimento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioAtendimentoRepository extends JpaRepository<HorarioAtendimento, Long> {

    List<HorarioAtendimento> findByProfissionalIdOrderByDiaSemanaAscHoraInicioAsc(Long idProfissional);
}
