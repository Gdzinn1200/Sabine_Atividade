# Casos de teste — entrega final de 27/11/2026

Projeto: **AgendaPro** — Equipe 1 — Fábrica de Software
Recorte testado: acesso por perfil, guarda de senha, notificações, relatórios e recuperação de
senha (RF02, RF14 a RF18, RNF01 e RNF02), somados a tudo que já foi entregue em 20/10 e 06/11
Versão testada: commit da tag `entrega-27-11-2026`
Data de execução dos testes automatizados: 27/11/2026

Evidências desta entrega:

- `docs/evidencias/execucao_testes_regras_27-11-2026.txt` — execução dos 65 casos automatizados
- `docs/evidencias/compilacao_projeto_27-11-2026.txt` — compilação do projeto completo, sem erros

## 1. Testes automatizados das regras de domínio

Executados por `mvn test` (JUnit 5) e também pelo executor sem dependências
`ferramentas/TesteRegras.java`, que roda com o JDK puro:

```
javac -d build $(find src/main/java/br/edu/cesumar/agendapro/dominio/regras -name "*.java") ferramentas/TesteRegras.java
java -cp build TesteRegras
```

Resultado da última execução: **65 casos, 65 aprovados, 0 reprovados.**

Os 33 casos das entregas anteriores (CT01 a CT33) continuam na suíte e seguem aprovados; estão
descritos em `casos_de_teste_20-10-2026.md` e `casos_de_teste_06-11-2026.md`. Abaixo estão os
casos criados nesta entrega.

### Acesso por perfil (RNF02 e RN13)

| Caso | Requisito / regra | Verificação | Resultado esperado | Situação |
| --- | --- | --- | --- | --- |
| CT34 | RNF02 | Administrador abre `/relatorios` | Permitido | Aprovado |
| CT35 | RNF02, RN13 | Cliente abre `/relatorios` | Recusado: relatório é só do administrador | Aprovado |
| CT36 | RNF02 | Profissional abre `/agenda` | Permitido | Aprovado |
| CT37 | RNF02 | Profissional abre `/usuarios` | Recusado: administração de contas é do administrador | Aprovado |
| CT38 | RNF02 | Cliente abre `/agendamentos/novo` | Permitido: o cliente marca o próprio atendimento | Aprovado |
| CT39 | RNF02 | Visitante sem sessão abre `/login` e `/` | `/login` permitido, `/` recusado | Aprovado |
| CT40 | RNF02 | Todo item do menu de cada perfil passa pelo filtro | Nenhum item oferecido sem permissão | Aprovado |
| CT41 | RNF02 | Quadro de permissões conferido contra a lista de áreas | Toda área tem ao menos um perfil e o quadro acompanha o filtro | Aprovado |

> **Por que CT40 e CT41 existem:** o erro clássico de controle de acesso é o menu e o filtro
> discordarem — uma tela some do menu mas continua acessível pela URL, ou o contrário. No AgendaPro
> os dois saem da **mesma** lista de áreas da classe `RegrasAcesso`, e estes dois casos é que
> garantem que continue assim.

### Guarda da senha e recuperação (RNF01 e RF18)

| Caso | Requisito / regra | Verificação | Resultado esperado | Situação |
| --- | --- | --- | --- | --- |
| CT42 | RNF01 | Senha cifrada e procurada em texto no registro gravado | Registro em formato PBKDF2; a senha não aparece | Aprovado |
| CT43 | RNF01 | Conferência com a senha correta | Aceita | Aprovado |
| CT44 | RNF01 | Conferência com a senha errada (só a caixa das letras muda) | Recusada | Aprovado |
| CT45 | RNF01 | Mesma senha cifrada duas vezes | Registros diferentes: o sal muda a cada cadastro | Aprovado |
| CT46 | RNF01 | Senha com 3 caracteres | Recusada: mínimo de 6 | Aprovado |
| CT47 | RNF01 | Confirmação diferente da senha | Recusada | Aprovado |
| CT48 | RF18 | Dois tokens de recuperação gerados em sequência | Valores diferentes | Aprovado |

### Notificações (RF14, RF15 e RN11)

| Caso | Requisito / regra | Condição | Verificação | Situação |
| --- | --- | --- | --- | --- |
| CT49 | RN11 | Atendimento confirmado daqui a 20 horas | Lembrete deve ser enviado | Aprovado |
| CT50 | RN11 | Mesmo atendimento, lembrete já registrado | Não envia de novo | Aprovado |
| CT51 | RN11 | Atendimento daqui a 5 dias | Ainda não envia | Aprovado |
| CT52 | RN11 | Atendimento cancelado daqui a 20 horas | Não envia | Aprovado |
| CT53 | RN11 | Atendimento que já passou | Não envia | Aprovado |
| CT54 | RN11 | Lista com os cinco casos acima | A rotina separa exatamente um atendimento | Aprovado |
| CT55 | RF14 | Mudança para Cancelado e para Confirmado | Cada uma gera o tipo de aviso correspondente | Aprovado |
| CT56 | RF14 | Destinatário vazio e destinatário nulo | Nenhum dos dois é notificado | Aprovado |

### Relatórios (RF16 e RN13)

| Caso | Requisito / regra | Condição | Verificação | Situação |
| --- | --- | --- | --- | --- |
| CT57 | RF16 | Cinco atendimentos, um fora do período | O relatório considera apenas os quatro do período | Aprovado |
| CT58 | RF16 | Dois concluídos (45,00 e 30,00), um cancelado e uma falta | Faturamento de 75,00: só o concluído entra | Aprovado |
| CT59 | RF16 | Mesmo conjunto | Total 4, concluídos 2, cancelados 1, faltas 1 | Aprovado |
| CT60 | RF16 | Mesmo conjunto | Taxa de comparecimento de 67% | Aprovado |
| CT61 | RF16 | Mesmo conjunto | Agrupamento por profissional: Bruno com 3 atendimentos | Aprovado |
| CT62 | RF16 | Mesmo conjunto | Agrupamento por serviço: Corte com 3 atendimentos | Aprovado |
| CT63 | RN13 | Data inicial depois da final | Período recusado | Aprovado |
| CT64 | RN13 | Período de 400 dias | Recusado: o limite é 365 dias | Aprovado |
| CT65 | RN13 | Período de 01/11 a 30/11 | Aceito | Aprovado |

> **Decisão registrada em CT60:** a taxa de comparecimento compara os concluídos com os
> atendimentos que chegaram à hora marcada — concluídos mais faltas. O cancelamento feito com
> antecedência libera o horário para outro cliente e por isso não conta contra a taxa. Sem essa
> definição o número ficaria ambíguo: a mesma agenda daria 50% ou 67% conforme a leitura.

## 2. Testes manuais da entrega final

Executar com o banco carregado por `banco-de-dados/script_criacao.sql` e
`banco-de-dados/dados_demonstracao.sql`. As três contas de demonstração usam a senha `123456`:
`admin@agendapro.com` (administrador), `ana@agendapro.com` (profissional) e
`marina@email.com` (cliente).

| Caso | Requisito | Passo | Resultado esperado | Resultado observado | Situação |
| --- | --- | --- | --- | --- | --- |
| CTM09 | RF02 | Entrar com `admin@agendapro.com` e senha `123456` | Painel do administrador com o menu completo | Preencher após executar | Preencher após executar |
| CTM10 | RF02, RNF01 | Entrar com a senha errada | Mensagem genérica, sem dizer se o e-mail existe | Preencher após executar | Preencher após executar |
| CTM11 | RNF02 | Entrar como `marina@email.com` e digitar `/relatorios` na barra de endereço | Tela "Esta área não faz parte do seu perfil" | Preencher após executar | Preencher após executar |
| CTM12 | RNF02 | Entrar como `ana@agendapro.com` e abrir a Agenda | Só os atendimentos da própria profissional, com o filtro travado | Preencher após executar | Preencher após executar |
| CTM13 | RNF02 | Como cliente, abrir "Meus atendimentos" | Somente os atendimentos da Marina | Preencher após executar | Preencher após executar |
| CTM14 | RF14 | Marcar um atendimento e abrir Notificações | Registro de confirmação para o e-mail do cliente | Preencher após executar | Preencher após executar |
| CTM15 | RN11 | Clicar em "Rodar a rotina agora" em Notificações | Lembretes dos atendimentos das próximas 24 horas, uma vez cada | Preencher após executar | Preencher após executar |
| CTM16 | RF16 | Gerar o relatório do mês corrente | Totais, faturamento e as duas tabelas de agrupamento | Preencher após executar | Preencher após executar |
| CTM17 | RF16 | Clicar em "Baixar CSV" | Arquivo CSV com uma linha por atendimento do período | Preencher após executar | Preencher após executar |
| CTM18 | RN13 | Pedir um relatório com a data inicial depois da final | Mensagem de período inválido, sem gerar o relatório | Preencher após executar | Preencher após executar |
| CTM19 | RF18 | Pedir recuperação de senha para `marina@email.com` | Link de redefinição gerado e registrado em Notificações | Preencher após executar | Preencher após executar |
| CTM20 | RF18 | Usar o mesmo link de redefinição duas vezes | A segunda vez é recusada: o link serve uma vez só | Preencher após executar | Preencher após executar |
| CTM21 | RF17 | Cadastrar um usuário novo e desativar a conta | Conta criada e, depois de desativada, o login é recusado | Preencher após executar | Preencher após executar |
| CTM22 | RNF05, RNF06 | Abrir o sistema em uma janela de 320 px de largura | Telas legíveis, sem rolagem horizontal | Preencher após executar | Preencher após executar |

> As colunas de resultado observado são preenchidas no momento da execução, junto com a captura de
> tela. Um caso não executado não pode ser marcado como aprovado.

## 3. Defeitos encontrados e corrigidos nesta entrega

Os dois defeitos abaixo foram encontrados pelos próprios testes automatizados, antes da entrega, e
já estão corrigidos. Ficam registrados porque mostram o teste cumprindo o papel dele.

| Defeito | Como apareceu | Correção | Caso que protege |
| --- | --- | --- | --- |
| A tela de relatórios avisava que o período não podia passar de 365 dias, mas a regra não conferia esse limite: um pedido de 400 dias passava | CT64 reprovou na primeira execução | `RegrasRelatorio.validarPeriodo` passou a recusar período acima de `DIAS_MAXIMO_PERIODO` (365) | CT64 |
| A taxa de comparecimento não tinha definição escrita, e o teste foi escrito com uma leitura diferente da implementação (50% contra 67%) | CT60 reprovou na primeira execução | A definição foi fixada no código e no caso de teste: concluídos sobre concluídos mais faltas, sem contar cancelamentos | CT60 |

## 4. Situações-limite tratadas

| Situação-limite | Tratamento na implementação | Caso que protege |
| --- | --- | --- |
| O menu esconder uma tela que continua acessível digitando a URL | Menu e filtro leem a mesma lista de áreas de `RegrasAcesso` | CT40 |
| Duas contas com a mesma senha gerarem o mesmo registro no banco, permitindo descobrir uma pela outra | Cada cadastro usa um sal próprio de 16 bytes | CT45 |
| O cliente receber o mesmo lembrete várias vezes se a rotina rodar mais de uma vez no dia | A rotina só considera quem ainda não tem lembrete registrado para aquele atendimento | CT50 e CT54 |
| O cancelamento derrubar a taxa de comparecimento de quem avisou com antecedência | Cancelado não entra no cálculo da taxa | CT60 |
| O link de recuperação continuar valendo depois de usado ou depois do prazo | O pedido guarda `expira_em` e `usado`, conferidos antes de redefinir | CTM20 |
| O relatório varrer a base inteira e travar a tela | O período é limitado a 365 dias | CT64 |

Se um teste falhar nas execuções seguintes, registrar aqui o defeito com descrição, forma de
reproduzir, responsável e situação, repetindo o teste após a correção.
