package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.regras.ErroValidacao;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Traduz os erros das regras de dominio para o que a tela exibe (RNF05). */
public final class Mensagens {

    private Mensagens() {
    }

    /** Mapa campo -> mensagem, usado pelos formularios. */
    public static Map<String, String> porCampo(ValidacaoException e) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (ErroValidacao erro : e.getErros()) {
            mapa.putIfAbsent(erro.campo(), erro.mensagemCompleta());
        }
        return mapa;
    }

    /** Texto unico, usado quando a tela nao tem um campo para destacar. */
    public static String resumo(ValidacaoException e) {
        return e.getErros().stream()
                .map(ErroValidacao::mensagemCompleta)
                .collect(Collectors.joining(" "));
    }
}
