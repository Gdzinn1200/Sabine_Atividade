package br.edu.cesumar.agendapro.dominio.regras;

/**
 * Situacoes possiveis de um atendimento. A sequencia permitida entre elas e a
 * RN09, implementada em {@link RegrasSituacao}.
 */
public enum SituacaoAgendamento {

    AGENDADO("Agendado"),
    CONFIRMADO("Confirmado"),
    EM_ATENDIMENTO("Em atendimento"),
    CONCLUIDO("Concluido"),
    CANCELADO("Cancelado"),
    NAO_COMPARECEU("Nao compareceu");

    private final String rotulo;

    SituacaoAgendamento(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Situacoes que ainda ocupam o horario na agenda do profissional (RN01). */
    public boolean isAtiva() {
        return this == AGENDADO || this == CONFIRMADO || this == EM_ATENDIMENTO;
    }

    /** Situacoes finais: nao podem mais ser alteradas (RN09). */
    public boolean isFinal() {
        return !isAtiva();
    }
}
