package br.edu.cesumar.agendapro.dominio.regras;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Regras do cadastro de profissionais (RF03) e das faixas de atendimento (RF05).
 * Classe de dominio puro.
 *
 * Regras aplicadas:
 *  - campos obrigatorios (RNF05)
 *  - RN06: o profissional precisa executar ao menos um servico
 *  - RN02: faixas de horario coerentes e sem sobreposicao no mesmo dia
 */
public final class RegrasProfissional {

    private RegrasProfissional() {
    }

    public record FaixaExistente(Long id, int diaSemana, LocalTime inicio, LocalTime fim) {
    }

    public static List<ErroValidacao> validar(String nome, String telefone, Collection<Long> servicos) {
        List<ErroValidacao> erros = new ArrayList<>();

        String nomeLimpo = texto(nome);
        if (nomeLimpo.length() < 3) {
            erros.add(ErroValidacao.de("nome", "Informe o nome do profissional."));
        } else if (nomeLimpo.length() > 120) {
            erros.add(ErroValidacao.de("nome", "O nome deve ter no maximo 120 caracteres."));
        }

        String digitos = texto(telefone).replaceAll("\\D", "");
        if (digitos.isEmpty()) {
            erros.add(ErroValidacao.de("telefone", "Informe o telefone de contato."));
        } else if (digitos.length() < 10 || digitos.length() > 11) {
            erros.add(ErroValidacao.de("telefone", "Informe um telefone valido com DDD, com 10 ou 11 digitos."));
        }

        if (servicos == null || servicos.isEmpty()) {
            erros.add(ErroValidacao.de("servicos",
                    "Selecione ao menos um servico executado pelo profissional.", "RN06"));
        }
        return erros;
    }

    public static void validarOuFalhar(String nome, String telefone, Collection<Long> servicos) {
        List<ErroValidacao> erros = validar(nome, telefone, servicos);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    /** RN02: a faixa precisa ser coerente e nao pode se sobrepor a outra do mesmo dia. */
    public static List<ErroValidacao> validarFaixa(int diaSemana, LocalTime inicio, LocalTime fim,
                                                   List<FaixaExistente> doDia) {
        List<ErroValidacao> erros = new ArrayList<>();
        if (diaSemana < 1 || diaSemana > 7) {
            erros.add(ErroValidacao.de("diaSemana", "Selecione um dia da semana valido."));
        }
        if (inicio == null || fim == null) {
            erros.add(ErroValidacao.de("faixa", "Informe a hora inicial e a hora final."));
            return erros;
        }
        if (!fim.isAfter(inicio)) {
            erros.add(ErroValidacao.de("faixa", "A hora final deve ser posterior a inicial."));
            return erros;
        }
        for (FaixaExistente f : doDia) {
            if (f.diaSemana() != diaSemana) {
                continue;
            }
            if (inicio.isBefore(f.fim()) && f.inicio().isBefore(fim)) {
                erros.add(ErroValidacao.de("faixa",
                        "A faixa se sobrepoe a outra ja cadastrada (" + f.inicio() + " as " + f.fim() + ").",
                        "RN02"));
                return erros;
            }
        }
        return erros;
    }

    public static void validarFaixaOuFalhar(int diaSemana, LocalTime inicio, LocalTime fim,
                                            List<FaixaExistente> doDia) {
        List<ErroValidacao> erros = validarFaixa(diaSemana, inicio, fim, doDia);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    /** RN02: o bloqueio precisa ter periodo coerente. */
    public static List<ErroValidacao> validarBloqueio(LocalTime inicio, LocalTime fim) {
        List<ErroValidacao> erros = new ArrayList<>();
        if (inicio == null || fim == null) {
            erros.add(ErroValidacao.de("bloqueio", "Informe a hora inicial e a hora final do bloqueio."));
        } else if (!fim.isAfter(inicio)) {
            erros.add(ErroValidacao.de("bloqueio", "A hora final deve ser posterior a inicial."));
        }
        return erros;
    }

    public static void validarBloqueioOuFalhar(LocalTime inicio, LocalTime fim) {
        List<ErroValidacao> erros = validarBloqueio(inicio, fim);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
