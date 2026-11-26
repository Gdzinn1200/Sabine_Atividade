package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.RecuperacaoSenha;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, Long> {

    Optional<RecuperacaoSenha> findByToken(String token);
}
