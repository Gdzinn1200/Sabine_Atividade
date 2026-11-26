package br.edu.cesumar.agendapro.servico;

import br.edu.cesumar.agendapro.dominio.Agendamento;
import br.edu.cesumar.agendapro.dominio.Notificacao;
import br.edu.cesumar.agendapro.dominio.RecuperacaoSenha;
import br.edu.cesumar.agendapro.dominio.regras.RegrasNotificacao;
import br.edu.cesumar.agendapro.dominio.regras.TipoNotificacao;
import br.edu.cesumar.agendapro.repositorio.AgendamentoRepository;
import br.edu.cesumar.agendapro.repositorio.NotificacaoRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mensagens enviadas ao cliente (RF14 e RF15).
 *
 * Nesta versao o envio e simulado: o sistema registra a mensagem que seria
 * enviada, com destinatario, assunto e data e hora. A troca por um servidor
 * SMTP real altera apenas o metodo {@link #entregar}, sem mexer nas regras.
 */
@Service
public class NotificacaoService {

    private static final DateTimeFormatter QUANDO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    private final NotificacaoRepository repositorio;
    private final AgendamentoRepository agendamentos;

    public NotificacaoService(NotificacaoRepository repositorio, AgendamentoRepository agendamentos) {
        this.repositorio = repositorio;
        this.agendamentos = agendamentos;
    }

    public List<Notificacao> ultimas() {
        return repositorio.findTop100ByOrderByDataHoraEnvioDesc();
    }

    public List<Notificacao> doAgendamento(Long idAgendamento) {
        return repositorio.findByAgendamentoIdOrderByDataHoraEnvio(idAgendamento);
    }

    /** RF14: avisa o cliente quando o agendamento e criado, alterado ou cancelado. */
    @Transactional
    public void registrar(Agendamento agendamento, TipoNotificacao tipo) {
        if (agendamento == null || agendamento.getCliente() == null) {
            return;
        }
        String destinatario = agendamento.getCliente().getEmail();
        if (!RegrasNotificacao.podeNotificar(destinatario)) {
            return;
        }
        String quando = agendamento.getDataHoraInicio().format(QUANDO);
        String assunto = switch (tipo) {
            case CONFIRMACAO -> "Seu atendimento esta marcado para " + quando;
            case LEMBRETE -> "Lembrete: seu atendimento e " + quando;
            case CANCELAMENTO -> "Seu atendimento de " + quando + " foi cancelado";
            case RECUPERACAO_SENHA -> "Redefinicao de senha";
        };
        entregar(agendamento, tipo, destinatario, assunto);
    }

    /** RF18: envia o link temporario de redefinicao de senha. */
    @Transactional
    public void registrarRecuperacaoSenha(RecuperacaoSenha pedido) {
        if (pedido == null || pedido.getUsuario() == null) {
            return;
        }
        entregar(null, TipoNotificacao.RECUPERACAO_SENHA, pedido.getUsuario().getEmail(),
                "Link para redefinir sua senha, valido por "
                        + UsuarioService.HORAS_VALIDADE_TOKEN + " horas");
    }

    /**
     * RF15 e RN11: rotina do lembrete. Executa automaticamente uma vez por dia
     * e tambem pode ser disparada pela tela, para a demonstracao.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public int enviarLembretes() {
        LocalDateTime agora = LocalDateTime.now();

        List<RegrasNotificacao.Candidato> candidatos = new ArrayList<>();
        for (Agendamento a : agendamentos.findAll()) {
            boolean jaTem = repositorio.existsByAgendamentoIdAndTipo(a.getId(), TipoNotificacao.LEMBRETE);
            candidatos.add(new RegrasNotificacao.Candidato(a.getId(), a.getDataHoraInicio(),
                    a.getSituacao(), jaTem));
        }

        List<Long> aEnviar = RegrasNotificacao.lembretesAEnviar(candidatos, agora);
        int enviados = 0;
        for (Long id : aEnviar) {
            Agendamento agendamento = agendamentos.findById(id).orElse(null);
            if (agendamento == null) {
                continue;
            }
            String destinatario = agendamento.getCliente().getEmail();
            if (!RegrasNotificacao.podeNotificar(destinatario)) {
                continue;
            }
            entregar(agendamento, TipoNotificacao.LEMBRETE, destinatario,
                    "Lembrete: seu atendimento e " + agendamento.getDataHoraInicio().format(QUANDO));
            enviados++;
        }
        return enviados;
    }

    /**
     * Ponto unico de entrega. Hoje apenas grava o registro da mensagem; para
     * enviar de verdade, basta acrescentar aqui a chamada ao servidor de e-mail.
     */
    private void entregar(Agendamento agendamento, TipoNotificacao tipo,
                          String destinatario, String assunto) {
        Notificacao notificacao = new Notificacao();
        notificacao.setAgendamento(agendamento);
        notificacao.setTipo(tipo);
        notificacao.setDestinatario(destinatario);
        notificacao.setAssunto(assunto);
        notificacao.setDataHoraEnvio(LocalDateTime.now());
        notificacao.setEnviada(true);
        repositorio.save(notificacao);
    }
}
