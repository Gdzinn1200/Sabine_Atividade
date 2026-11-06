package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.Profissional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    List<Profissional> findAllByOrderByNome();

    List<Profissional> findByAtivoTrueOrderByNome();
}
