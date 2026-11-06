package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByDataHoraInicioBetweenOrderByDataHoraInicio(LocalDateTime inicio, LocalDateTime fim);

    List<Agendamento> findByClienteIdOrderByDataHoraInicioDesc(Long idCliente);
}
