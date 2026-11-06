import br.edu.cesumar.agendapro.dominio.regras.ErroValidacao;
import br.edu.cesumar.agendapro.dominio.regras.RegrasAgenda;
import br.edu.cesumar.agendapro.dominio.regras.RegrasCliente;
import br.edu.cesumar.agendapro.dominio.regras.RegrasProfissional;
import br.edu.cesumar.agendapro.dominio.regras.RegrasServico;
import br.edu.cesumar.agendapro.dominio.regras.RegrasSituacao;
import br.edu.cesumar.agendapro.dominio.regras.SituacaoAgendamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Executor dos testes das regras de dominio, sem Maven e sem banco. Util para
 * rodar e registrar evidencia em qualquer maquina com o JDK instalado:
 *
 *   javac -d build $(find src/main/java/br/edu/cesumar/agendapro/dominio/regras -name "*.java") ferramentas/TesteRegras.java
 *   java -cp build TesteRegras
 *
 * Os mesmos casos existem como testes JUnit em src/test (mvn test).
 */
public class TesteRegras {

    private static int total = 0;
    private static int falhas = 0;

    // Base fixa usada nos casos de agenda: a profissional 1 atende das 08:00 as 18:00
    // de segunda a sexta e tem um bloqueio de almoco na data de referencia.
    private static final Long PROF = 1L;
    private static final Long CLIENTE = 1L;

    public static void main(String[] args) {
        System.out.println("AgendaPro - execucao dos testes de regra de negocio");
        System.out.println("Data da execucao: " + LocalDateTime.now().withNano(0));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-7s %-22s %-44s %s%n", "Caso", "Requisito / regra", "Verificacao", "Resultado");
        System.out.println("-------------------------------------------------------------------------------");

        cadastros();
        agenda();
        situacao();

        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("Total: %d caso(s) | Aprovados: %d | Reprovados: %d%n",
                total, total - falhas, falhas);
        if (falhas > 0) {
            System.exit(1);
        }
    }

    // =================================================================
    // Cadastros de cliente e de servico (entrega de 20/10)
    // =================================================================
    private static void cadastros() {
        List<RegrasCliente.ClienteExistente> clientes = List.of(
                new RegrasCliente.ClienteExistente(1L, "marina@email.com", "(42) 99876-1122", true),
                new RegrasCliente.ClienteExistente(2L, "rafael@email.com", "(42) 99712-3040", false));

        verificar("CT01", "RF01", "Cadastrar cliente com dados validos",
                RegrasCliente.validar(null, "Camila Reis", "(42) 99500-7711", "camila@email.com",
                        LocalDate.of(1999, 7, 25), clientes).isEmpty());

        var obrigatorios = RegrasCliente.validar(null, "", "", "", null, clientes);
        verificar("CT02", "RF01 / RNF05", "Recusar nome e telefone vazios",
                obrigatorios.size() == 2
                        && temCampo(obrigatorios, "nome") && temCampo(obrigatorios, "telefone"));

        var emailRepetido = RegrasCliente.validar(null, "Outra Pessoa", "(42) 90000-0001",
                "marina@email.com", null, clientes);
        verificar("CT03", "RN07", "Recusar e-mail repetido de cliente ativo",
                emailRepetido.size() == 1 && temRegra(emailRepetido, "RN07"));

        verificar("CT04", "RN07", "Permitir telefone de cliente inativo",
                RegrasCliente.validar(null, "Novo Cliente", "(42) 99712-3040", "novo@email.com",
                        null, clientes).isEmpty());

        verificar("CT05", "RN07", "Editar o proprio cadastro sem acusar duplicidade",
                RegrasCliente.validar(1L, "Marina Souza", "(42) 99876-1122", "marina@email.com",
                        null, clientes).isEmpty());

        var formato = RegrasCliente.validar(null, "Teste Formato", "(42) 90000-0002",
                "email-invalido", null, clientes);
        verificar("CT06", "RNF05", "Recusar e-mail em formato invalido",
                formato.size() == 1 && temCampo(formato, "email"));

        List<RegrasServico.ServicoExistente> servicos = List.of(
                new RegrasServico.ServicoExistente(1L, "Corte feminino", true));

        verificar("CT07", "RF04", "Cadastrar servico com dados validos",
                RegrasServico.validar(null, "Coloracao", 90, new BigDecimal("180.00"), servicos).isEmpty());

        var semDuracao = RegrasServico.validar(null, "Teste", null, BigDecimal.TEN, servicos);
        var duracaoZero = RegrasServico.validar(null, "Teste", 0, BigDecimal.TEN, servicos);
        verificar("CT08", "RN03", "Recusar servico sem duracao valida",
                temRegra(semDuracao, "RN03") && temRegra(duracaoZero, "RN03"));

        var nomeRepetido = RegrasServico.validar(null, "corte feminino", 40,
                new BigDecimal("70.00"), servicos);
        verificar("CT09", "RN14", "Recusar nome de servico repetido",
                nomeRepetido.size() == 1 && temRegra(nomeRepetido, "RN14"));

        verificar("CT10", "RN03", "Calcular o horario final pela duracao",
                RegrasServico.horarioFinal(LocalTime.of(9, 0), 90).equals(LocalTime.of(10, 30))
                        && RegrasServico.horarioFinal(LocalTime.of(9, 0), 40).equals(LocalTime.of(9, 40)));

        var precoNegativo = RegrasServico.validar(null, "Teste preco", 30,
                new BigDecimal("-1.00"), servicos);
        verificar("CT11", "RNF05", "Recusar preco negativo",
                precoNegativo.size() == 1 && temCampo(precoNegativo, "preco"));
    }

    // =================================================================
    // Disponibilidade e agendamento (entrega de 06/11)
    // =================================================================
    private static void agenda() {
        LocalDate dia = proximaQuarta();
        LocalDateTime agora = LocalDateTime.of(dia.minusDays(1), LocalTime.of(10, 0));

        List<RegrasAgenda.Faixa> faixas = List.of(
                new RegrasAgenda.Faixa(PROF, RegrasAgenda.diaSemana(dia), LocalTime.of(8, 0), LocalTime.of(18, 0)));
        List<RegrasAgenda.Bloqueio> bloqueios = List.of(
                new RegrasAgenda.Bloqueio(PROF, dia, LocalTime.of(12, 0), LocalTime.of(13, 0), "Almoco"));
        List<RegrasAgenda.Compromisso> ocupados = List.of(
                new RegrasAgenda.Compromisso(10L, PROF, 2L, dia, LocalTime.of(9, 0), LocalTime.of(9, 40),
                        SituacaoAgendamento.CONFIRMADO),
                new RegrasAgenda.Compromisso(11L, PROF, 3L, dia, LocalTime.of(16, 0), LocalTime.of(16, 40),
                        SituacaoAgendamento.CANCELADO));
        RegrasAgenda.Agenda estado = new RegrasAgenda.Agenda(faixas, bloqueios, ocupados);

        verificar("CT12", "RN01", "Detectar sobreposicao de horario do profissional",
                RegrasAgenda.conflito(PROF, dia, LocalTime.of(9, 20), LocalTime.of(10, 0),
                        ocupados, null).isPresent());

        verificar("CT13", "RN01", "Aceitar atendimentos encostados, sem sobreposicao",
                RegrasAgenda.conflito(PROF, dia, LocalTime.of(9, 40), LocalTime.of(10, 20),
                        ocupados, null).isEmpty());

        verificar("CT14", "RN01", "Horario de atendimento cancelado volta a ficar livre",
                RegrasAgenda.conflito(PROF, dia, LocalTime.of(16, 0), LocalTime.of(16, 40),
                        ocupados, null).isEmpty());

        verificar("CT15", "RN02", "Recusar horario fora da faixa de atendimento",
                !RegrasAgenda.dentroDoHorario(PROF, dia, LocalTime.of(7, 0), LocalTime.of(7, 40), faixas)
                        && RegrasAgenda.dentroDoHorario(PROF, dia, LocalTime.of(14, 0), LocalTime.of(14, 40), faixas));

        verificar("CT16", "RN02", "Detectar bloqueio de agenda no periodo",
                RegrasAgenda.bloqueio(PROF, dia, LocalTime.of(12, 20), LocalTime.of(13, 0),
                        bloqueios).isPresent());

        List<RegrasAgenda.Horario> grade = RegrasAgenda.horariosDisponiveis(PROF, dia, 40,
                estado, agora, null);
        boolean gradeCoerente = !grade.isEmpty()
                && grade.stream().anyMatch(h -> h.inicio().equals(LocalTime.of(9, 0)) && !h.livre())
                && grade.stream().anyMatch(h -> h.inicio().equals(LocalTime.of(14, 0)) && h.livre())
                && grade.stream().anyMatch(h -> h.inicio().equals(LocalTime.of(12, 0)) && !h.livre());
        verificar("CT17", "RF07", "Grade marca ocupados, bloqueados e livres",
                gradeCoerente);

        LocalDateTime depois = LocalDateTime.of(dia, LocalTime.of(15, 0));
        List<RegrasAgenda.Horario> gradeTarde = RegrasAgenda.horariosDisponiveis(PROF, dia, 40,
                estado, depois, null);
        verificar("CT18", "RN04", "Horarios ja passados nao sao oferecidos",
                gradeTarde.stream().filter(h -> h.inicio().isBefore(LocalTime.of(15, 0)))
                        .noneMatch(RegrasAgenda.Horario::livre));

        RegrasAgenda.Pedido naoExecuta = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, false, dia, LocalTime.of(14, 0), 40);
        verificar("CT19", "RN06", "Recusar profissional que nao executa o servico",
                temRegra(RegrasAgenda.validarAgendamento(naoExecuta, estado, agora), "RN06"));

        List<RegrasAgenda.Compromisso> tresFuturos = List.of(
                compromisso(20L, dia, LocalTime.of(8, 0), SituacaoAgendamento.AGENDADO),
                compromisso(21L, dia, LocalTime.of(10, 0), SituacaoAgendamento.AGENDADO),
                compromisso(22L, dia, LocalTime.of(11, 0), SituacaoAgendamento.CONFIRMADO));
        RegrasAgenda.Agenda cheia = new RegrasAgenda.Agenda(faixas, List.of(), tresFuturos);
        RegrasAgenda.Pedido quarto = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, true, dia, LocalTime.of(14, 0), 40);
        verificar("CT20", "RN08", "Recusar o quarto agendamento futuro do cliente",
                RegrasAgenda.agendamentosFuturosAtivos(CLIENTE, tresFuturos, agora, null) == 3
                        && temRegra(RegrasAgenda.validarAgendamento(quarto, cheia, agora), "RN08"));

        LocalDate hoje = LocalDate.now();
        List<RegrasAgenda.Compromisso> faltas = List.of(
                falta(30L, hoje.minusDays(10)),
                falta(31L, hoje.minusDays(40)),
                falta(32L, hoje.minusDays(80)),
                falta(33L, hoje.minusDays(200)));
        verificar("CT21", "RN10", "Contar faltas na janela de 90 dias e bloquear o cliente",
                RegrasAgenda.faltasRecentes(CLIENTE, faltas, hoje) == 3
                        && RegrasAgenda.deveBloquearPorFaltas(CLIENTE, faltas, hoje));

        RegrasAgenda.Pedido bloqueado = new RegrasAgenda.Pedido(null, CLIENTE, true,
                PROF, true, true, dia, LocalTime.of(14, 0), 40);
        verificar("CT22", "RN10", "Recusar agendamento de cliente bloqueado por faltas",
                temRegra(RegrasAgenda.validarAgendamento(bloqueado, estado, agora), "RN10"));

        RegrasAgenda.Pedido retroativo = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, true, dia.minusDays(10), LocalTime.of(14, 0), 40);
        verificar("CT23", "RN04", "Recusar agendamento em data ja passada",
                temRegra(RegrasAgenda.validarAgendamento(retroativo, estado, agora), "RN04"));

        RegrasAgenda.Pedido sobreposto = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, true, dia, LocalTime.of(9, 20), 40);
        verificar("CT24", "RN01", "Recusar agendamento sobreposto a outro atendimento",
                temRegra(RegrasAgenda.validarAgendamento(sobreposto, estado, agora), "RN01"));

        RegrasAgenda.Pedido valido = new RegrasAgenda.Pedido(null, CLIENTE, false,
                PROF, true, true, dia, LocalTime.of(14, 0), 40);
        verificar("CT25", "RF08", "Aceitar agendamento que atende a todas as regras",
                RegrasAgenda.validarAgendamento(valido, estado, agora).isEmpty());

        List<RegrasProfissional.FaixaExistente> faixaDoDia = List.of(
                new RegrasProfissional.FaixaExistente(1L, 4, LocalTime.of(8, 0), LocalTime.of(18, 0)));
        var invertida = RegrasProfissional.validarFaixa(4, LocalTime.of(10, 0), LocalTime.of(9, 0), List.of());
        var sobreposta = RegrasProfissional.validarFaixa(4, LocalTime.of(17, 0), LocalTime.of(19, 0), faixaDoDia);
        verificar("CT26", "RF05 / RN02", "Recusar faixa invertida e faixa sobreposta",
                invertida.size() == 1 && sobreposta.size() == 1 && temRegra(sobreposta, "RN02"));
    }

    // =================================================================
    // Situacao do atendimento e cancelamento
    // =================================================================
    private static void situacao() {
        verificar("CT27", "RN09", "Permitir a sequencia Agendado - Confirmado - Em atendimento - Concluido",
                RegrasSituacao.transicaoPermitida(SituacaoAgendamento.AGENDADO, SituacaoAgendamento.CONFIRMADO)
                        && RegrasSituacao.transicaoPermitida(SituacaoAgendamento.CONFIRMADO,
                                SituacaoAgendamento.EM_ATENDIMENTO)
                        && RegrasSituacao.transicaoPermitida(SituacaoAgendamento.EM_ATENDIMENTO,
                                SituacaoAgendamento.CONCLUIDO));

        verificar("CT28", "RN09", "Recusar salto de Agendado direto para Concluido",
                temRegra(RegrasSituacao.validarTransicao(SituacaoAgendamento.AGENDADO,
                        SituacaoAgendamento.CONCLUIDO), "RN09"));

        verificar("CT29", "RN09", "Recusar alteracao de atendimento em situacao final",
                SituacaoAgendamento.CONCLUIDO.isFinal()
                        && temRegra(RegrasSituacao.validarTransicao(SituacaoAgendamento.CONCLUIDO,
                                SituacaoAgendamento.CONFIRMADO), "RN09"));

        LocalDateTime agora = LocalDateTime.of(2026, 11, 10, 8, 0);
        LocalDateTime daquiDuasHoras = agora.plusHours(2);
        LocalDateTime daquiSeisHoras = agora.plusHours(6);

        verificar("CT30", "RN05", "Recusar cancelamento do cliente com menos de 4 horas",
                temRegra(RegrasSituacao.validarCancelamento(SituacaoAgendamento.CONFIRMADO,
                        daquiDuasHoras, agora, RegrasSituacao.Perfil.CLIENTE, "Solicitacao do cliente"), "RN05"));

        verificar("CT31", "RN05", "Permitir cancelamento do cliente com mais de 4 horas",
                RegrasSituacao.validarCancelamento(SituacaoAgendamento.CONFIRMADO, daquiSeisHoras, agora,
                        RegrasSituacao.Perfil.CLIENTE, "Solicitacao do cliente").isEmpty());

        verificar("CT32", "RN05", "Permitir cancelamento pelo estabelecimento a qualquer momento",
                RegrasSituacao.validarCancelamento(SituacaoAgendamento.CONFIRMADO, daquiDuasHoras, agora,
                        RegrasSituacao.Perfil.ADMINISTRADOR, "Indisponibilidade do profissional").isEmpty());

        verificar("CT33", "RF10 / RNF05", "Exigir o motivo do cancelamento",
                temCampo(RegrasSituacao.validarCancelamento(SituacaoAgendamento.AGENDADO, daquiSeisHoras,
                        agora, RegrasSituacao.Perfil.ADMINISTRADOR, "  "), "motivo"));
    }

    // =================================================================
    // apoio
    // =================================================================
    private static RegrasAgenda.Compromisso compromisso(Long id, LocalDate dia, LocalTime inicio,
                                                        SituacaoAgendamento situacao) {
        return new RegrasAgenda.Compromisso(id, PROF, CLIENTE, dia, inicio, inicio.plusMinutes(40), situacao);
    }

    private static RegrasAgenda.Compromisso falta(Long id, LocalDate dia) {
        return new RegrasAgenda.Compromisso(id, PROF, CLIENTE, dia, LocalTime.of(9, 0), LocalTime.of(9, 40),
                SituacaoAgendamento.NAO_COMPARECEU);
    }

    /** Uma quarta-feira futura, para os casos nao dependerem do dia em que sao executados. */
    private static LocalDate proximaQuarta() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() != java.time.DayOfWeek.WEDNESDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    private static boolean temCampo(List<ErroValidacao> erros, String campo) {
        return erros.stream().anyMatch(e -> campo.equals(e.campo()));
    }

    private static boolean temRegra(List<ErroValidacao> erros, String regra) {
        return erros.stream().anyMatch(e -> regra.equals(e.regra()));
    }

    private static void verificar(String caso, String requisito, String descricao, boolean passou) {
        total++;
        if (!passou) {
            falhas++;
        }
        System.out.printf("%-7s %-22s %-44s %s%n", caso, requisito, descricao,
                passou ? "APROVADO" : "REPROVADO");
    }
}
