package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.Cliente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByAtivoTrueOrderByNome();

    List<Cliente> findByAtivoFalseOrderByNome();

    @Query("select c from Cliente c "
            + "where (:somenteAtivos = false or c.ativo = true) "
            + "and (lower(c.nome) like lower(concat('%', :termo, '%')) "
            + "     or lower(coalesce(c.email, '')) like lower(concat('%', :termo, '%')) "
            + "     or c.telefone like concat('%', :termo, '%')) "
            + "order by c.nome")
    List<Cliente> buscar(String termo, boolean somenteAtivos);
}
