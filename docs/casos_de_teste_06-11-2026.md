# Casos de teste — entrega de 06/11/2026

Projeto: **AgendaPro** — Equipe 1 — Fábrica de Software
Recorte testado: fluxo integrado de agendamento (RF03, RF05 a RF13) somado aos cadastros já entregues em 20/10 (RF01 e RF04)
Versão testada: commit da tag `entrega-06-11-2026`
Data de execução dos testes automatizados: 06/11/2026

## 1. Testes automatizados das regras de domínio

Executados por `mvn test` (JUnit 5) e também pelo executor sem dependências `ferramentas/TesteRegras.java`.
Evidência: `docs/evidencias/execucao_testes_regras_06-11-2026.txt`.

### Cadastros (casos mantidos da entrega de 20/10)

| Caso | Requisito / regra | Verificação | Resultado esperado | Situação |
| --- | --- | --- | --- | --- |
| CT01 | RF01 | Cliente com dados válidos | Aceito, sem erros | Aprovado |
| CT02 | RF01, RNF05 | Nome e telefone vazios | Dois erros, nos campos indicados | Aprovado |
| CT03 | RN07 | E-mail repetido de cliente ativo | Recusado citando a RN07 | Aprovado |
| CT04 | RN07 | Telefone de cliente inativo | Aceito: a regra vale só entre ativos | Aprovado |
| CT05 | RN07 | Edição do próprio cadastro | Aceito: o próprio registro não conflita | Aprovado |
| CT06 | RNF05 | E-mail em formato inválido | Recusado com mensagem de formato | Aprovado |
| CT07 | RF04 | Serviço com dados válidos | Aceito, sem erros | Aprovado |
| CT08 | RN03 | Serviço sem duração e com duração zero | Recusado nos dois casos citando a RN03 | Aprovado |
| CT09 | RN14 | Nome de serviço repetido entre ativos | Recusado citando a RN14 | Aprovado |
| CT10 | RN03 | Término de 09:00 + 90 min e + 40 min | 10:30 e 09:40 | Aprovado |
| CT11 | RNF05 | Preço negativo | Recusado no campo preço | Aprovado |

### Disponibilidade e agendamento (novos nesta entrega)

| Caso | Requisito / regra | Condição de execução | Verificação | Resultado esperado | Situação |
| --- | --- | --- | --- | --- | --- |
| CT12 | RN01 | Profissional com atendimento das 09:00 às 09:40 | Pedido das 09:20 às 10:00 | Conflito detectado | Aprovado |
| CT13 | RN01 | Mesma agenda | Pedido das 09:40 às 10:20 | Sem conflito: atendimentos encostados são válidos | Aprovado |
| CT14 | RN01 | Atendimento das 16:00 cancelado | Pedido das 16:00 às 16:40 | Sem conflito: cancelado libera o horário | Aprovado |
| CT15 | RN02 | Faixa das 08:00 às 18:00 | Pedidos das 07:00 e das 14:00 | O das 07:00 é recusado, o das 14:00 é aceito | Aprovado |
| CT16 | RN02 | Bloqueio de almoço das 12:00 às 13:00 | Pedido das 12:20 às 13:00 | Bloqueio detectado | Aprovado |
| CT17 | RF07 | Agenda com ocupado, bloqueio e horários livres | Montagem da grade do dia | 09:00 ocupado, 12:00 bloqueado, 14:00 livre | Aprovado |
| CT18 | RN04 | Consulta feita às 15:00 do próprio dia | Montagem da grade | Nenhum horário anterior às 15:00 aparece livre | Aprovado |
| CT19 | RN06 | Profissional que não executa o serviço | Validação do agendamento | Recusado citando a RN06 | Aprovado |
| CT20 | RN08 | Cliente com 3 agendamentos futuros ativos | Quarto pedido | Contagem igual a 3 e recusa citando a RN08 | Aprovado |
| CT21 | RN10 | 3 faltas em 90 dias e 1 falta há 200 dias | Contagem de faltas | Conta 3 e indica bloqueio | Aprovado |
| CT22 | RN10 | Cliente marcado como bloqueado | Validação do agendamento | Recusado citando a RN10 | Aprovado |
| CT23 | RN04 | Data 10 dias no passado | Validação do agendamento | Recusado citando a RN04 | Aprovado |
| CT24 | RN01 | Horário sobreposto a atendimento ativo | Validação do agendamento | Recusado citando a RN01 | Aprovado |
| CT25 | RF08 | Cliente liberado, profissional habilitado, horário livre | Validação do agendamento | Nenhum erro: agendamento aceito | Aprovado |
| CT26 | RF05, RN02 | Faixa das 08:00 às 18:00 já cadastrada | Faixa invertida e faixa das 17:00 às 19:00 | As duas recusadas; a sobreposta cita a RN02 | Aprovado |

### Situação do atendimento e cancelamento (novos nesta entrega)

| Caso | Requisito / regra | Verificação | Resultado esperado | Situação |
| --- | --- | --- | --- | --- |
| CT27 | RN09 | Agendado → Confirmado → Em atendimento → Concluído | Todas as transições permitidas | Aprovado |
| CT28 | RN09 | Agendado → Concluído | Recusado citando a RN09 | Aprovado |
| CT29 | RN09 | Concluído, Cancelado e Não compareceu | Reconhecidos como finais; alteração recusada | Aprovado |
| CT30 | RN05 | Cliente cancelando 2 horas antes | Recusado citando a RN05 | Aprovado |
| CT31 | RN05 | Cliente cancelando 6 horas antes | Aceito | Aprovado |
| CT32 | RN05 | Estabelecimento cancelando 2 horas antes | Aceito | Aprovado |
| CT33 | RF10, RNF05 | Cancelamento sem motivo | Recusado no campo motivo | Aprovado |

**Resumo:** 33 casos executados, 33 aprovados, 0 reprovados.

## 2. Testes manuais na aplicação

Executados no navegador, com a aplicação em `http://localhost:8080`, o banco criado por
`banco-de-dados/script_criacao.sql` e a carga de `banco-de-dados/dados_demonstracao.sql`.
Cada caso recebe uma captura de tela identificada em `docs/evidencias/`.

### Fluxo integrado (o caminho completo do agendamento)

| Caso | Requisito / regra | Passos | Resultado esperado | Resultado observado | Situação |
| --- | --- | --- | --- | --- | --- |
| CTM09 | RF03, RN06 | Profissionais → Novo → preencher e marcar dois serviços → Salvar | Profissional listado com os serviços marcados | Preencher após executar | Preencher após executar |
| CTM10 | RF03, RN06 | Salvar profissional sem marcar nenhum serviço | Recusa citando a RN06 | Preencher após executar | Preencher após executar |
| CTM11 | RF05, RN02 | Horários → escolher profissional → adicionar faixa com fim antes do início | Recusa com mensagem de faixa inválida | Preencher após executar | Preencher após executar |
| CTM12 | RF05 | Adicionar faixa válida de segunda, 08:00 às 18:00 | Faixa aparece na lista do profissional | Preencher após executar | Preencher após executar |
| CTM13 | RF06, RN02 | Registrar bloqueio das 12:00 às 13:00 na data de amanhã | Bloqueio listado | Preencher após executar | Preencher após executar |
| CTM14 | RF07, RN06 | Novo agendamento → trocar o serviço | A lista de profissionais muda para quem executa o serviço | Preencher após executar | Preencher após executar |
| CTM15 | RF07, RN01, RN02, RN04 | Ver horários da data de amanhã | Horários ocupados, bloqueados e passados aparecem riscados | Preencher após executar | Preencher após executar |
| CTM16 | RF08, RN03 | Clicar em um horário livre | Agendamento gravado; abre o detalhe com situação Agendado e término calculado | Preencher após executar | Preencher após executar |
| CTM17 | RN01 | Voltar ao novo agendamento na mesma data e profissional | O horário recém-ocupado aparece riscado | Preencher após executar | Preencher após executar |
| CTM18 | RF12 | Agenda → filtrar pela data do agendamento | O atendimento aparece na lista com a situação correta | Preencher após executar | Preencher após executar |
| CTM19 | RF11, RN09 | No detalhe: Confirmar → Iniciar → Concluir | A situação avança e, ao concluir, as ações desaparecem | Preencher após executar | Preencher após executar |
| CTM20 | RN09 | Tentar alterar um atendimento concluído | Aviso de situação final | Preencher após executar | Preencher após executar |
| CTM21 | RF09 | Abrir outro agendamento → Remarcar → escolher novo horário | Horário alterado e registro no histórico | Preencher após executar | Preencher após executar |
| CTM22 | RF10, RN05 | Cancelar informando o solicitante como Cliente, faltando menos de 4 horas | Recusa citando a RN05 | Preencher após executar | Preencher após executar |
| CTM23 | RF10 | Cancelar como Estabelecimento, com motivo | Cancelado; horário volta a ficar livre na agenda | Preencher após executar | Preencher após executar |
| CTM24 | RNF10 | Conferir o histórico no detalhe do agendamento | Todas as mudanças com data, hora e motivo | Preencher após executar | Preencher após executar |
| CTM25 | RF13 | Clientes → Histórico de Marina Souza | Atendimentos anteriores com valor e situação final | Preencher após executar | Preencher após executar |
| CTM26 | RN10 | Novo agendamento para Patrícia Nunes (3 faltas) | Recusa citando a RN10 | Preencher após executar | Preencher após executar |
| CTM27 | RNF12 | Parar e reiniciar a aplicação; abrir a agenda | Os agendamentos gravados continuam disponíveis | Preencher após executar | Preencher após executar |

### Casos de cadastro mantidos de 20/10

| Caso | Requisito / regra | Ação | Resultado esperado | Resultado observado | Situação |
| --- | --- | --- | --- | --- | --- |
| CTM01 | RF01 | Cadastrar cliente com dados válidos | Sucesso e cliente na listagem | Preencher após executar | Preencher após executar |
| CTM02 | RF01, RNF05 | Salvar o formulário vazio | Campos destacados; nada é gravado | Preencher após executar | Preencher após executar |
| CTM03 | RN07 | E-mail já usado por um cliente ativo | Mensagem citando a RN07 | Preencher após executar | Preencher após executar |
| CTM04 | RF01 | Buscar pelo nome parcial | Somente os clientes correspondentes | Preencher após executar | Preencher após executar |
| CTM05 | RN12 | Inativar e filtrar por "Todos" | Cliente aparece como inativo, não excluído | Preencher após executar | Preencher após executar |
| CTM06 | RF04, RN03 | Serviço com duração zero | Recusa citando a RN03 | Preencher após executar | Preencher após executar |
| CTM07 | RF04 | Cadastrar serviço válido | Serviço listado com duração e preço | Preencher após executar | Preencher após executar |
| CTM08 | RNF12 | Reiniciar a aplicação e abrir Clientes | Registros continuam disponíveis | Preencher após executar | Preencher após executar |

> As colunas de resultado observado são preenchidas no momento da execução, junto com a captura de
> tela. Um caso não executado não pode ser marcado como aprovado.

## 3. Defeitos e situações-limite

Nenhum defeito em aberto no fechamento desta entrega.

As situações-limite abaixo são as que mais facilmente passariam despercebidas na regra de conflito.
Cada uma foi tratada na implementação e tem um caso de teste que a protege contra regressão:

| Situação-limite | Tratamento na implementação | Caso que protege |
| --- | --- | --- |
| Dois atendimentos encostados (um termina 09:40, o outro começa 09:40) poderiam ser lidos como conflito e bloquear um horário válido | A comparação usa intervalo aberto no limite: há conflito apenas quando `inicio < fimOutro` **e** `inicioOutro < fim` | CT13 |
| Um atendimento cancelado poderia continuar ocupando o horário na agenda | A verificação considera somente as situações ativas (Agendado, Confirmado e Em atendimento) | CT14 |
| Na consulta feita no próprio dia, a grade poderia oferecer horários já passados | A RN04 é aplicada também na montagem da grade, e não apenas na gravação | CT18 |

Se um teste falhar nas execuções seguintes, registrar aqui o defeito com descrição, forma de
reproduzir, responsável e situação, repetindo o teste após a correção.
