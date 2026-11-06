package br.edu.cesumar.agendapro.dominio.regras;

import java.util.Collections;
import java.util.List;

/** Lancada quando uma operacao fere as validacoes ou as regras de negocio. */
public class ValidacaoException extends RuntimeException {

    private final transient List<ErroValidacao> erros;

    public ValidacaoException(List<ErroValidacao> erros) {
        super("Operacao recusada por " + erros.size() + " problema(s) de validacao");
        this.erros = List.copyOf(erros);
    }

    public List<ErroValidacao> getErros() {
        return Collections.unmodifiableList(erros);
    }
}
