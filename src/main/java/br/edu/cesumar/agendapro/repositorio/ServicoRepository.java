package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.Servico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findAllByOrderByNome();

    List<Servico> findByAtivoTrueOrderByNome();
}
