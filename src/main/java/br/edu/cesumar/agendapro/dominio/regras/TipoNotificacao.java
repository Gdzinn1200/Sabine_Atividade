package br.edu.cesumar.agendapro.dominio.regras;

/** Mensagens que o sistema envia ao cliente (RF14, RF15 e RF18). */
public enum TipoNotificacao {

    CONFIRMACAO("Confirmacao de agendamento"),
    LEMBRETE("Lembrete do atendimento"),
    CANCELAMENTO("Aviso de cancelamento"),
    RECUPERACAO_SENHA("Link para redefinir a senha");

    private final String rotulo;

    TipoNotificacao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
