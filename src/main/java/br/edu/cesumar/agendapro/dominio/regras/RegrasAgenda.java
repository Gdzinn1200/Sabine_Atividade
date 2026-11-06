package br.edu.cesumar.agendapro.dominio.regras;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Regras do agendamento. Classe de dominio puro: recebe os dados ja carregados e
 * devolve o resultado, sem depender de Spring nem de banco.
 *
 * Regras aplicadas:
 *  - RN01: um profissional nao pode ter dois atendimentos ativos sobrepostos
 *  - RN02: so dentro das faixas de atendimento e fora dos bloqueios
 *  - RN03: o termino sai da soma do inicio com a duracao do servico
 *  - RN04: nao se agenda em data ou hora ja passada
 *  - RN06: o profissional precisa estar ativo e executar o servico
 *  - RN08: no maximo 3 agendamentos futuros ativos por cliente
 *  - RN10: cliente com 3 faltas em 90 dias fica bloqueado
 */
public final class RegrasAgenda {

    public static final int PASSO_GRADE_MINUTOS = 20;
    public static final int LIMITE_FUTUROS = 3;
    public static final int FALTAS_PARA_BLOQUEIO = 3;
    public static final int JANELA_FALTAS_DIAS = 90;

    private RegrasAgenda() {
    }

    /** Atendimento ja gravado, usado nas verificacoes de conflito e de limite. */
    public record Compromisso(Long id, Long idProfissional, Long idCliente, LocalDate data,
                              LocalTime inicio, LocalTime fim, SituacaoAgendamento situacao) {

        public LocalDateTime inicioEm() {
            return LocalDateTime.of(data, inicio);
        }
    }

    /** Faixa de atendimento do profissional. diaSemana: 1 = domingo ... 7 = sabado. */
    public record Faixa(Long idProfissional, int diaSemana, LocalTime inicio, LocalTime fim) {
    }

    /** Periodo em que o profissional nao atende. */
    public record Bloqueio(Long idProfissional, LocalDate data, LocalTime inicio, LocalTime fim, String motivo) {
    }

    /** Um horario oferecido na tela, livre ou nao, com o motivo quando ocupado. */
    public record Horario(LocalTime inicio, LocalTime fim, boolean livre, String motivo) {

        public LocalTime getInicio() {
            return inicio;
        }

        public LocalTime getFim() {
            return fim;
        }

        public boolean isLivre() {
            return livre;
        }

        public String getMotivo() {
            return motivo;
        }

        /** Texto curto no formato HH:mm, usado nos botoes da grade. */
        public String getRotulo() {
            return String.format("%02d:%02d", inicio.getHour(), inicio.getMinute());
        }
    }

    /** Dados do agendamento que se quer gravar. */
    public record Pedido(Long idAgendamento, Long idCliente, boolean clienteBloqueado,
                         Long idProfissional, boolean profissionalAtivo, boolean executaServico,
                         LocalDate data, LocalTime inicio, int duracaoMinutos) {
    }

    /** Estado da agenda carregado do banco. */
    public record Agenda(List<Faixa> faixas, List<Bloqueio> bloqueios, List<Compromisso> compromissos) {

        public static Agenda vazia() {
            return new Agenda(List.of(), List.of(), List.of());
        }
    }

    // ---------------------------------------------------------------- RN03
    /** RN03: o termino e calculado, nunca informado pelo usuario. */
    public static LocalTime termino(LocalTime inicio, int duracaoMinutos) {
        return inicio.plusMinutes(duracaoMinutos);
    }

    /** Converte a data para o dia da semana usado no banco: 1 = domingo ... 7 = sabado. */
    public static int diaSemana(LocalDate data) {
        return data.getDayOfWeek().getValue() % 7 + 1;
    }

    // ---------------------------------------------------------------- RN01
    /** RN01: devolve o atendimento ativo que se sobrepoe ao periodo informado. */
    public static Optional<Compromisso> conflito(Long idProfissional, LocalDate data,
                                                 LocalTime inicio, LocalTime fim,
                                                 List<Compromisso> compromissos, Long idIgnorar) {
        for (Compromisso c : compromissos) {
            if (idIgnorar != null && idIgnorar.equals(c.id())) {
                continue;
            }
            if (!Objects.equals(c.idProfissional(), idProfissional) || !Objects.equals(c.data(), data)) {
                continue;
            }
            if (!c.situacao().isAtiva()) {
                continue;
            }
            if (inicio.isBefore(c.fim()) && c.inicio().isBefore(fim)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- RN02
    /** RN02: o periodo cabe inteiro dentro de uma faixa de atendimento do dia. */
    public static boolean dentroDoHorario(Long idProfissional, LocalDate data,
                                          LocalTime inicio, LocalTime fim, List<Faixa> faixas) {
        int dia = diaSemana(data);
        for (Faixa f : faixas) {
            if (!Objects.equals(f.idProfissional(), idProfissional) || f.diaSemana() != dia) {
                continue;
            }
            if (!inicio.isBefore(f.inicio()) && !fim.isAfter(f.fim())) {
                return true;
            }
        }
        return false;
    }

    /** RN02: devolve o bloqueio que atinge o periodo informado, se houver. */
    public static Optional<Bloqueio> bloqueio(Long idProfissional, LocalDate data,
                                              LocalTime inicio, LocalTime fim,
                                              List<Bloqueio> bloqueios) {
        for (Bloqueio b : bloqueios) {
            if (!Objects.equals(b.idProfissional(), idProfissional) || !Objects.equals(b.data(), data)) {
                continue;
            }
            if (inicio.isBefore(b.fim()) && b.inicio().isBefore(fim)) {
                return Optional.of(b);
            }
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- RN08 e RN10
    /** RN08: quantos agendamentos futuros ativos o cliente ja possui. */
    public static long agendamentosFuturosAtivos(Long idCliente, List<Compromisso> compromissos,
                                                 LocalDateTime agora, Long idIgnorar) {
        return compromissos.stream()
                .filter(c -> c.idCliente() != null && c.idCliente().equals(idCliente))
                .filter(c -> idIgnorar == null || !idIgnorar.equals(c.id()))
                .filter(c -> c.situacao() == SituacaoAgendamento.AGENDADO
                        || c.situacao() == SituacaoAgendamento.CONFIRMADO)
                .filter(c -> c.inicioEm().isAfter(agora))
                .count();
    }

    /** RN10: faltas do cliente dentro da janela de 90 dias. */
    public static long faltasRecentes(Long idCliente, List<Compromisso> compromissos, LocalDate hoje) {
        LocalDate limite = hoje.minusDays(JANELA_FALTAS_DIAS);
        return compromissos.stream()
                .filter(c -> c.idCliente() != null && c.idCliente().equals(idCliente))
                .filter(c -> c.situacao() == SituacaoAgendamento.NAO_COMPARECEU)
                .filter(c -> !c.data().isBefore(limite))
                .count();
    }

    /** RN10: o cliente atingiu o numero de faltas que gera bloqueio. */
    public static boolean deveBloquearPorFaltas(Long idCliente, List<Compromisso> compromissos, LocalDate hoje) {
        return faltasRecentes(idCliente, compromissos, hoje) >= FALTAS_PARA_BLOQUEIO;
    }

    // ---------------------------------------------------------------- grade
    /**
     * Monta os horarios do dia para um profissional e um servico, marcando os que
     * estao livres. Aplica RN01, RN02, RN03 e RN04.
     */
    public static List<Horario> horariosDisponiveis(Long idProfissional, LocalDate data, int duracaoMinutos,
                                                    Agenda agenda, LocalDateTime agora, Long idIgnorar) {
        List<Horario> resultado = new ArrayList<>();
        if (duracaoMinutos <= 0) {
            return resultado;
        }
        int dia = diaSemana(data);
        List<Faixa> doDia = new ArrayList<>(agenda.faixas().stream()
                .filter(f -> Objects.equals(f.idProfissional(), idProfissional) && f.diaSemana() == dia)
                .toList());
        doDia.sort(Comparator.comparing(Faixa::inicio));

        for (Faixa f : doDia) {
            LocalTime inicio = f.inicio();
            while (!termino(inicio, duracaoMinutos).isAfter(f.fim())) {
                LocalTime fim = termino(inicio, duracaoMinutos);
                String motivo = null;

                if (!LocalDateTime.of(data, inicio).isAfter(agora)) {
                    motivo = "Horario ja passou (RN04)";
                }
                if (motivo == null) {
                    Optional<Compromisso> c = conflito(idProfissional, data, inicio, fim,
                            agenda.compromissos(), idIgnorar);
                    if (c.isPresent()) {
                        motivo = "Ocupado (RN01)";
                    }
                }
                if (motivo == null) {
                    Optional<Bloqueio> b = bloqueio(idProfissional, data, inicio, fim, agenda.bloqueios());
                    if (b.isPresent()) {
                        String texto = b.get().motivo() == null || b.get().motivo().isBlank()
                                ? "periodo indisponivel" : b.get().motivo();
                        motivo = "Bloqueio: " + texto + " (RN02)";
                    }
                }
                resultado.add(new Horario(inicio, fim, motivo == null, motivo));
                inicio = inicio.plusMinutes(PASSO_GRADE_MINUTOS);
            }
        }
        return resultado;
    }

    public static long quantidadeLivre(List<Horario> horarios) {
        return horarios.stream().filter(Horario::livre).count();
    }

    // ---------------------------------------------------------------- validacao
    /** Aplica todas as regras do agendamento e devolve a lista de problemas. */
    public static List<ErroValidacao> validarAgendamento(Pedido pedido, Agenda agenda, LocalDateTime agora) {
        List<ErroValidacao> erros = new ArrayList<>();

        if (pedido.idCliente() == null) {
            erros.add(ErroValidacao.de("cliente", "Selecione o cliente do atendimento."));
        }
        if (pedido.idProfissional() == null) {
            erros.add(ErroValidacao.de("profissional", "Selecione o profissional do atendimento."));
        }
        if (pedido.data() == null || pedido.inicio() == null) {
            erros.add(ErroValidacao.de("horario", "Selecione a data e o horario do atendimento."));
        }
        if (pedido.duracaoMinutos() <= 0) {
            erros.add(ErroValidacao.de("servico", "Selecione um servico com duracao valida.", "RN03"));
        }
        if (!erros.isEmpty()) {
            return erros;
        }

        LocalTime fim = termino(pedido.inicio(), pedido.duracaoMinutos());

        if (!pedido.profissionalAtivo()) {
            erros.add(ErroValidacao.de("profissional",
                    "O profissional selecionado esta inativo.", "RN06"));
        }
        if (!pedido.executaServico()) {
            erros.add(ErroValidacao.de("profissional",
                    "O profissional selecionado nao executa este servico.", "RN06"));
        }
        if (!LocalDateTime.of(pedido.data(), pedido.inicio()).isAfter(agora)) {
            erros.add(ErroValidacao.de("data",
                    "Nao e possivel agendar em data ou hora anterior a atual.", "RN04"));
        }
        if (!dentroDoHorario(pedido.idProfissional(), pedido.data(), pedido.inicio(), fim, agenda.faixas())) {
            erros.add(ErroValidacao.de("horario",
                    "O horario esta fora da faixa de atendimento do profissional.", "RN02"));
        }
        Optional<Bloqueio> bloqueado = bloqueio(pedido.idProfissional(), pedido.data(),
                pedido.inicio(), fim, agenda.bloqueios());
        if (bloqueado.isPresent()) {
            String texto = bloqueado.get().motivo() == null || bloqueado.get().motivo().isBlank()
                    ? "periodo indisponivel" : bloqueado.get().motivo();
            erros.add(ErroValidacao.de("horario", "Periodo bloqueado: " + texto + ".", "RN02"));
        }
        Optional<Compromisso> ocupado = conflito(pedido.idProfissional(), pedido.data(),
                pedido.inicio(), fim, agenda.compromissos(), pedido.idAgendamento());
        if (ocupado.isPresent()) {
            erros.add(ErroValidacao.de("horario",
                    "O profissional ja possui atendimento das " + ocupado.get().inicio()
                            + " as " + ocupado.get().fim() + ".", "RN01"));
        }
        if (pedido.clienteBloqueado()) {
            erros.add(ErroValidacao.de("cliente",
                    "Cliente bloqueado por faltas. A liberacao e feita pelo administrador.", "RN10"));
        }
        if (agendamentosFuturosAtivos(pedido.idCliente(), agenda.compromissos(), agora,
                pedido.idAgendamento()) >= LIMITE_FUTUROS) {
            erros.add(ErroValidacao.de("cliente",
                    "O cliente ja possui " + LIMITE_FUTUROS + " agendamentos futuros ativos.", "RN08"));
        }
        return erros;
    }

    public static void validarAgendamentoOuFalhar(Pedido pedido, Agenda agenda, LocalDateTime agora) {
        List<ErroValidacao> erros = validarAgendamento(pedido, agenda, agora);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
}
