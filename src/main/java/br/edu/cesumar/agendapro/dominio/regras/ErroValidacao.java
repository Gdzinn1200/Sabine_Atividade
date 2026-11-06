package br.edu.cesumar.agendapro.dominio.regras;

/**
 * Um problema encontrado na validacao: o campo afetado, a mensagem exibida ao
 * usuario (RNF05) e a regra de negocio correspondente, quando houver.
 */
public record ErroValidacao(String campo, String mensagem, String regra) {

    public static ErroValidacao de(String campo, String mensagem) {
        return new ErroValidacao(campo, mensagem, null);
    }

    public static ErroValidacao de(String campo, String mensagem, String regra) {
        return new ErroValidacao(campo, mensagem, regra);
    }

    /** Mensagem pronta para a tela, com a regra entre parenteses quando existir. */
    public String mensagemCompleta() {
        return regra == null ? mensagem : mensagem + " (" + regra + ")";
    }
}
