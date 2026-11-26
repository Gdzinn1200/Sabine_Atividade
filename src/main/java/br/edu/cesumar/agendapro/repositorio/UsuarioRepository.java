package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    List<Usuario> findAllByOrderByNome();
}
