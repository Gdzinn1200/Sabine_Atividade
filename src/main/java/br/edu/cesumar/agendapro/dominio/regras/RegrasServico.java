package br.edu.cesumar.agendapro.dominio.regras;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de cadastro de servico (RF04). Classe de dominio puro.
 *
 * Regras aplicadas:
 *  - campos obrigatorios e faixas validas (RNF05)
 *  - RN03: a duracao e obrigatoria e maior que zero, porque dela sai o horario
 *    final do atendimento
 *  - RN14: nao podem existir dois servicos ativos com o mesmo nome
 */
public final class RegrasServico {

    public static final int DURACAO_MINIMA = 5;
    public static final int DURACAO_MAXIMA = 480;

    private RegrasServico() {
    }

    public record ServicoExistente(Long id, String nome, boolean ativo) {
    }

    public static List<ErroValidacao> validar(Long idAtual,
                                              String nome,
                                              Integer duracaoMinutos,
                                              BigDecimal preco,
                                              List<ServicoExistente> cadastrados) {
        List<ErroValidacao> erros = new ArrayList<>();

        String nomeLimpo = texto(nome);
        if (nomeLimpo.length() < 3) {
            erros.add(ErroValidacao.de("nome", "Informe o nome do servico."));
        } else if (nomeLimpo.length() > 80) {
            erros.add(ErroValidacao.de("nome", "O nome deve ter no maximo 80 caracteres."));
        }

        if (duracaoMinutos == null) {
            erros.add(ErroValidacao.de("duracaoMinutos",
                    "Informe a duracao do servico em minutos.", "RN03"));
        } else if (duracaoMinutos < DURACAO_MINIMA) {
            erros.add(ErroValidacao.de("duracaoMinutos",
                    "A duracao deve ser de no minimo " + DURACAO_MINIMA + " minutos.", "RN03"));
        } else if (duracaoMinutos > DURACAO_MAXIMA) {
            erros.add(ErroValidacao.de("duracaoMinutos",
                    "A duracao deve ser de no maximo " + DURACAO_MAXIMA + " minutos.", "RN03"));
        }

        if (preco == null) {
            erros.add(ErroValidacao.de("preco", "Informe o preco do servico."));
        } else if (preco.signum() < 0) {
            erros.add(ErroValidacao.de("preco", "O preco nao pode ser negativo."));
        }

        for (ServicoExistente s : cadastrados) {
            if (!s.ativo() || (idAtual != null && idAtual.equals(s.id()))) {
                continue;
            }
            if (nomeLimpo.equalsIgnoreCase(texto(s.nome()))) {
                erros.add(ErroValidacao.de("nome",
                        "Ja existe um servico ativo com este nome.", "RN14"));
            }
        }
        return erros;
    }

    public static void validarOuFalhar(Long idAtual,
                                       String nome,
                                       Integer duracaoMinutos,
                                       BigDecimal preco,
                                       List<ServicoExistente> cadastrados) {
        List<ErroValidacao> erros = validar(idAtual, nome, duracaoMinutos, preco, cadastrados);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    /**
     * RN03: o horario final do atendimento e calculado pela soma do horario
     * inicial com a duracao do servico.
     */
    public static java.time.LocalTime horarioFinal(java.time.LocalTime inicio, int duracaoMinutos) {
        return inicio.plusMinutes(duracaoMinutos);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
