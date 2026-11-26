package br.edu.cesumar.agendapro.dominio.regras;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Guarda e confere a senha do usuario (RNF01).
 *
 * A senha nunca e gravada em texto legivel: e derivada com PBKDF2-HMAC-SHA256,
 * algoritmo de hash com sal do proprio JDK, usando um sal aleatorio por usuario
 * e 120.000 iteracoes. O formato gravado e
 * {@code pbkdf2$<iteracoes>$<sal em base64>$<hash em base64>}, o que permite
 * aumentar as iteracoes no futuro sem invalidar as senhas ja existentes.
 */
public final class Senha {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SAL = 16;
    private static final int TAMANHO_HASH = 256;
    private static final int MINIMO_CARACTERES = 6;
    private static final SecureRandom SORTEIO = new SecureRandom();

    private Senha() {
    }

    /** Gera o registro que vai para a coluna senha_hash. */
    public static String cifrar(String senhaDigitada) {
        byte[] sal = new byte[TAMANHO_SAL];
        SORTEIO.nextBytes(sal);
        byte[] hash = derivar(senhaDigitada, sal, ITERACOES);
        return "pbkdf2$" + ITERACOES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Confere a senha digitada contra o registro gravado. */
    public static boolean confere(String senhaDigitada, String registro) {
        if (senhaDigitada == null || registro == null) {
            return false;
        }
        String[] partes = registro.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }
        try {
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] obtido = derivar(senhaDigitada, sal, iteracoes);
            return igualdadeConstante(esperado, obtido);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** O registro gravado nao expoe a senha em texto legivel. */
    public static boolean registroCifrado(String registro) {
        return registro != null && registro.startsWith("pbkdf2$") && registro.split("\\$").length == 4;
    }

    /** Validacao da senha escolhida pelo usuario (RNF05). */
    public static List<ErroValidacao> validar(String senha, String confirmacao) {
        List<ErroValidacao> erros = new ArrayList<>();
        String s = senha == null ? "" : senha;
        if (s.length() < MINIMO_CARACTERES) {
            erros.add(ErroValidacao.de("senha",
                    "A senha deve ter no minimo " + MINIMO_CARACTERES + " caracteres."));
        }
        if (confirmacao != null && !s.equals(confirmacao)) {
            erros.add(ErroValidacao.de("confirmacao", "A confirmacao nao confere com a senha informada."));
        }
        return erros;
    }

    public static void validarOuFalhar(String senha, String confirmacao) {
        List<ErroValidacao> erros = validar(senha, confirmacao);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    private static byte[] derivar(String senha, byte[] sal, int iteracoes) {
        try {
            KeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, TAMANHO_HASH);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel cifrar a senha", e);
        }
    }

    /** Comparacao de tempo constante, para nao vazar informacao pelo tempo de resposta. */
    private static boolean igualdadeConstante(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diferenca = 0;
        for (int i = 0; i < a.length; i++) {
            diferenca |= a[i] ^ b[i];
        }
        return diferenca == 0;
    }

    /** Usado apenas na geracao do token de recuperacao de senha (RF18). */
    public static String tokenAleatorio() {
        byte[] bytes = new byte[24];
        SORTEIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String textoUtf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
