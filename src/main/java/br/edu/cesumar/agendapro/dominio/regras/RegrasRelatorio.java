package br.edu.cesumar.agendapro.dominio.regras;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consolidacao dos atendimentos para os relatorios (RF16). Classe de dominio
 * puro: recebe os atendimentos do periodo e devolve os totais, sem consultar o
 * banco. O acesso a estes numeros e restrito ao administrador pela RN13,
 * verificada em {@link RegrasAcesso}.
 */
public final class RegrasRelatorio {

    private RegrasRelatorio() {
    }

    /** Um atendimento do periodo, no minimo necessario para os totais. */
    public record Atendimento(LocalDate data, String profissional, String servico,
                              SituacaoAgendamento situacao, BigDecimal valor) {
    }

    /** Totais do periodo inteiro. */
    public record Resumo(long total, long concluidos, long cancelados, long faltas,
                         BigDecimal faturamento, int taxaComparecimento) {

        public long getTotal() {
            return total;
        }

        public long getConcluidos() {
            return concluidos;
        }

        public long getCancelados() {
            return cancelados;
        }

        public long getFaltas() {
            return faltas;
        }

        public BigDecimal getFaturamento() {
            return faturamento;
        }

        public int getTaxaComparecimento() {
            return taxaComparecimento;
        }
    }

    /** Uma linha agrupada, por profissional ou por servico. */
    public record Linha(String rotulo, long total, long concluidos, long cancelados,
                        long faltas, BigDecimal faturamento) {

        public String getRotulo() {
            return rotulo;
        }

        public long getTotal() {
            return total;
        }

        public long getConcluidos() {
            return concluidos;
        }

        public long getCancelados() {
            return cancelados;
        }

        public long getFaltas() {
            return faltas;
        }

        public BigDecimal getFaturamento() {
            return faturamento;
        }
    }

    /** O periodo informado e valido? */
    /** Teto do intervalo consultado, para o relatorio nao varrer a base inteira (RN13). */
    public static final int DIAS_MAXIMO_PERIODO = 365;

    public static List<ErroValidacao> validarPeriodo(LocalDate de, LocalDate ate) {
        List<ErroValidacao> erros = new ArrayList<>();
        if (de == null || ate == null) {
            erros.add(ErroValidacao.de("periodo", "Informe a data inicial e a data final."));
            return erros;
        }
        if (de.isAfter(ate)) {
            erros.add(ErroValidacao.de("periodo", "A data inicial nao pode ser posterior a final."));
            return erros;
        }
        long dias = java.time.temporal.ChronoUnit.DAYS.between(de, ate);
        if (dias > DIAS_MAXIMO_PERIODO) {
            erros.add(ErroValidacao.de("periodo", "O periodo nao pode passar de "
                    + DIAS_MAXIMO_PERIODO + " dias. Escolha um intervalo menor."));
        }
        return erros;
    }

    public static List<Atendimento> noPeriodo(List<Atendimento> atendimentos, LocalDate de, LocalDate ate) {
        List<Atendimento> filtrados = new ArrayList<>();
        for (Atendimento a : atendimentos) {
            if (a.data() == null || a.data().isBefore(de) || a.data().isAfter(ate)) {
                continue;
            }
            filtrados.add(a);
        }
        return filtrados;
    }

    public static Resumo resumir(List<Atendimento> atendimentos) {
        long concluidos = contar(atendimentos, SituacaoAgendamento.CONCLUIDO);
        long cancelados = contar(atendimentos, SituacaoAgendamento.CANCELADO);
        long faltas = contar(atendimentos, SituacaoAgendamento.NAO_COMPARECEU);
        BigDecimal faturamento = somar(atendimentos);

        // Cancelamento com antecedencia libera o horario, por isso nao entra na conta:
        // a taxa compara quem compareceu com quem chegou a hora do atendimento.
        long encerrados = concluidos + faltas;
        int taxa = encerrados == 0 ? 0 : (int) Math.round(concluidos * 100.0 / encerrados);

        return new Resumo(atendimentos.size(), concluidos, cancelados, faltas, faturamento, taxa);
    }

    public static List<Linha> porProfissional(List<Atendimento> atendimentos) {
        return agrupar(atendimentos, true);
    }

    public static List<Linha> porServico(List<Atendimento> atendimentos) {
        return agrupar(atendimentos, false);
    }

    private static List<Linha> agrupar(List<Atendimento> atendimentos, boolean porProfissional) {
        Map<String, List<Atendimento>> grupos = new LinkedHashMap<>();
        for (Atendimento a : atendimentos) {
            String chave = porProfissional ? a.profissional() : a.servico();
            grupos.computeIfAbsent(chave == null ? "-" : chave, k -> new ArrayList<>()).add(a);
        }
        List<Linha> linhas = new ArrayList<>();
        for (Map.Entry<String, List<Atendimento>> e : grupos.entrySet()) {
            List<Atendimento> itens = e.getValue();
            linhas.add(new Linha(e.getKey(), itens.size(),
                    contar(itens, SituacaoAgendamento.CONCLUIDO),
                    contar(itens, SituacaoAgendamento.CANCELADO),
                    contar(itens, SituacaoAgendamento.NAO_COMPARECEU),
                    somar(itens)));
        }
        linhas.sort((x, y) -> Long.compare(y.concluidos(), x.concluidos()));
        return linhas;
    }

    private static long contar(List<Atendimento> atendimentos, SituacaoAgendamento situacao) {
        return atendimentos.stream().filter(a -> a.situacao() == situacao).count();
    }

    /** Somente atendimentos concluidos entram no faturamento. */
    private static BigDecimal somar(List<Atendimento> atendimentos) {
        BigDecimal total = BigDecimal.ZERO;
        for (Atendimento a : atendimentos) {
            if (a.situacao() == SituacaoAgendamento.CONCLUIDO && a.valor() != null) {
                total = total.add(a.valor());
            }
        }
        return total;
    }
}
