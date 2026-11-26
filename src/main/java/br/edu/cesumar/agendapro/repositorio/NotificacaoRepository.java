package br.edu.cesumar.agendapro.repositorio;

import br.edu.cesumar.agendapro.dominio.Notificacao;
import br.edu.cesumar.agendapro.dominio.regras.TipoNotificacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findTop100ByOrderByDataHoraEnvioDesc();

    List<Notificacao> findByAgendamentoIdOrderByDataHoraEnvio(Long idAgendamento);

    boolean existsByAgendamentoIdAndTipo(Long idAgendamento, TipoNotificacao tipo);
}
