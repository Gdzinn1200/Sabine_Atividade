package br.edu.cesumar.agendapro.dominio;

import br.edu.cesumar.agendapro.dominio.regras.TipoNotificacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Mensagem enviada ao cliente (RF14, RF15 e RF18). */
@Entity
@Table(name = "notificacao")
public class Notificacao {

    private static final java.time.format.DateTimeFormatter QUANDO =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacao")
    private Long id;

    /** Nulo nas mensagens que nao se referem a um atendimento, como a de senha. */
    @ManyToOne
    @JoinColumn(name = "id_agendamento")
    private Agendamento agendamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoNotificacao tipo;

    @Column(name = "destinatario", nullable = false, length = 120)
    private String destinatario;

    @Column(name = "assunto", length = 160)
    private String assunto;

    @Column(name = "data_hora_envio")
    private LocalDateTime dataHoraEnvio;

    @Column(name = "enviada", nullable = false)
    private boolean enviada = false;

    public String getQuandoFormatado() {
        return dataHoraEnvio == null ? "-" : dataHoraEnvio.format(QUANDO);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public LocalDateTime getDataHoraEnvio() {
        return dataHoraEnvio;
    }

    public void setDataHoraEnvio(LocalDateTime dataHoraEnvio) {
        this.dataHoraEnvio = dataHoraEnvio;
    }

    public boolean isEnviada() {
        return enviada;
    }

    public void setEnviada(boolean enviada) {
        this.enviada = enviada;
    }
}
