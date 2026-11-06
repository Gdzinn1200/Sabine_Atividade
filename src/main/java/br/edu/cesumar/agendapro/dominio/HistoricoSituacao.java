package br.edu.cesumar.agendapro.dominio;

import br.edu.cesumar.agendapro.dominio.regras.SituacaoAgendamento;
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

/** Registro de cada mudanca de situacao do atendimento (RNF10). */
@Entity
@Table(name = "historico_situacao")
public class HistoricoSituacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historico")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_agendamento", nullable = false)
    private Agendamento agendamento;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_anterior", length = 20)
    private SituacaoAgendamento situacaoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_nova", nullable = false, length = 20)
    private SituacaoAgendamento situacaoNova;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(name = "motivo", length = 255)
    private String motivo;

    private static final java.time.format.DateTimeFormatter QUANDO =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String getQuandoFormatado() {
        return dataHora == null ? "" : dataHora.format(QUANDO);
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

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public SituacaoAgendamento getSituacaoAnterior() {
        return situacaoAnterior;
    }

    public void setSituacaoAnterior(SituacaoAgendamento situacaoAnterior) {
        this.situacaoAnterior = situacaoAnterior;
    }

    public SituacaoAgendamento getSituacaoNova() {
        return situacaoNova;
    }

    public void setSituacaoNova(SituacaoAgendamento situacaoNova) {
        this.situacaoNova = situacaoNova;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
