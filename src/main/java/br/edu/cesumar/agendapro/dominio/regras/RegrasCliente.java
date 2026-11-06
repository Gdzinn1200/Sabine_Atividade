package br.edu.cesumar.agendapro.dominio.regras;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de cadastro de cliente (RF01). Classe de dominio puro, sem dependencia
 * de framework ou de banco, para permitir teste direto.
 *
 * Regras aplicadas:
 *  - campos obrigatorios e formato (RNF05)
 *  - RN07: nao podem existir dois clientes ativos com o mesmo e-mail ou telefone
 */
public final class RegrasCliente {

    private RegrasCliente() {
    }

    /** Dados minimos de um cliente ja gravado, usados na verificacao da RN07. */
    public record ClienteExistente(Long id, String email, String telefone, boolean ativo) {
    }

    public static List<ErroValidacao> validar(Long idAtual,
                                              String nome,
                                              String telefone,
                                              String email,
                                              LocalDate nascimento,
                                              List<ClienteExistente> cadastrados) {
        List<ErroValidacao> erros = new ArrayList<>();

        String nomeLimpo = texto(nome);
        if (nomeLimpo.length() < 3) {
            erros.add(ErroValidacao.de("nome", "Informe o nome completo do cliente."));
        } else if (nomeLimpo.length() > 120) {
            erros.add(ErroValidacao.de("nome", "O nome deve ter no maximo 120 caracteres."));
        }

        String telefoneLimpo = texto(telefone);
        String telefoneDigitos = somenteDigitos(telefoneLimpo);
        if (telefoneDigitos.isEmpty()) {
            erros.add(ErroValidacao.de("telefone", "Informe o telefone de contato."));
        } else if (telefoneDigitos.length() < 10 || telefoneDigitos.length() > 11) {
            erros.add(ErroValidacao.de("telefone", "Informe um telefone valido com DDD, com 10 ou 11 digitos."));
        }

        String emailLimpo = texto(email).toLowerCase();
        if (!emailLimpo.isEmpty() && !emailValido(emailLimpo)) {
            erros.add(ErroValidacao.de("email", "Informe um e-mail valido, no formato nome@dominio.com."));
        }

        if (nascimento != null && nascimento.isAfter(LocalDate.now())) {
            erros.add(ErroValidacao.de("nascimento", "A data de nascimento nao pode ser futura."));
        }

        for (ClienteExistente c : cadastrados) {
            if (!c.ativo() || mesmoRegistro(idAtual, c.id())) {
                continue;
            }
            if (!emailLimpo.isEmpty() && emailLimpo.equalsIgnoreCase(texto(c.email()))) {
                erros.add(ErroValidacao.de("email",
                        "Ja existe um cliente ativo com este e-mail.", "RN07"));
            }
            if (!telefoneDigitos.isEmpty() && telefoneDigitos.equals(somenteDigitos(c.telefone()))) {
                erros.add(ErroValidacao.de("telefone",
                        "Ja existe um cliente ativo com este telefone.", "RN07"));
            }
        }
        return erros;
    }

    /** Valida e interrompe a operacao quando houver qualquer erro. */
    public static void validarOuFalhar(Long idAtual,
                                       String nome,
                                       String telefone,
                                       String email,
                                       LocalDate nascimento,
                                       List<ClienteExistente> cadastrados) {
        List<ErroValidacao> erros = validar(idAtual, nome, telefone, email, nascimento, cadastrados);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    public static String somenteDigitos(String valor) {
        return texto(valor).replaceAll("\\D", "");
    }

    private static boolean emailValido(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static boolean mesmoRegistro(Long idAtual, Long idExistente) {
        return idAtual != null && idAtual.equals(idExistente);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
