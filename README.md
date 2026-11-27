# AgendaPro

**Sistema web de agendamento para pequenos negócios.**

Projeto desenvolvido pela **Equipe 1** nas disciplinas de **Imersão Profissional — Projeto de Software** (1º bimestre) e **Fábrica de Software** (2º bimestre), na Universidade Cesumar.

## 🔗 Links rápidos para a apresentação

| O que | Onde |
| --- | --- |
| **Ver o sistema funcionando** (protótipo, abre no navegador) | **https://gdzinn1200.github.io/Sabine_Atividade/prototipos/agendapro_funcional.html** |
| Slides da apresentação final | [`docs/apresentacao/slides-apresentacao-final.pdf`](docs/apresentacao/slides-apresentacao-final.pdf) |
| Slides do banco de dados | [`docs/apresentacao/slides-banco-de-dados.pdf`](docs/apresentacao/slides-banco-de-dados.pdf) |
| Documento da entrega final | [`docs/2bim-03-conclusao-e-apresentacao-final.pdf`](docs/2bim-03-conclusao-e-apresentacao-final.pdf) |
| Resultado dos 65 testes | [`docs/evidencias/execucao_testes_regras_27-11-2026.txt`](docs/evidencias/execucao_testes_regras_27-11-2026.txt) |
| Script do banco | [`banco-de-dados/script_criacao.sql`](banco-de-dados/script_criacao.sql) |
| Modelos do banco | [conceitual](banco-de-dados/conceitual.png) · [lógico 1](banco-de-dados/logico1.png) · [lógico 2](banco-de-dados/logico2.png) |

Contas de demonstração do protótipo, todas com a senha `123456`: `admin@agendapro.com` (administrador), `ana@agendapro.com` (profissional) e `marina@email.com` (cliente).

---

> **Estado atual: sistema completo.** Os 18 requisitos funcionais previstos estão implementados — do cadastro ao relatório, passando por autenticação com perfis, agendamento com todas as regras aplicadas, notificações e recuperação de senha. Os dados ficam em MySQL e as regras de negócio são cobertas por **65 casos de teste automatizados**.

---

## Sumário

1. [Sobre o projeto](#1-sobre-o-projeto)
2. [A equipe](#2-a-equipe)
3. [O que o sistema faz](#3-o-que-o-sistema-faz)
4. [Regras de negócio](#4-regras-de-negócio)
5. [Tecnologias](#5-tecnologias)
6. [Como executar](#6-como-executar)
7. [Como executar os testes](#7-como-executar-os-testes)
8. [Estrutura do repositório](#8-estrutura-do-repositório)
9. [Documentação das entregas](#9-documentação-das-entregas)
10. [Rastreabilidade](#10-rastreabilidade)
11. [Como a equipe trabalha](#11-como-a-equipe-trabalha)
12. [O que ficou pronto e o que viria depois](#12-o-que-ficou-pronto-e-o-que-viria-depois)
13. [Licença](#13-licença)

---

## 1. Sobre o projeto

### O problema

Pequenos negócios que atendem por horário marcado — salões de beleza, barbearias, clínicas, estúdios e oficinas — costumam controlar a agenda por telefone, mensagens ou anotações em papel. O registro fica espalhado e ninguém enxerga o dia inteiro de uma vez, o que gera três problemas recorrentes:

- **Horários em conflito** — dois clientes marcados no mesmo horário com o mesmo profissional, ou um encaixe que invade o atendimento seguinte.
- **Faltas e esquecimentos** — ninguém confirma nem lembra o cliente, e o horário vago só é descoberto na hora.
- **Informação perdida** — não há histórico de quem foi atendido, quando, por qual profissional e por quanto.

Quem sente na prática: **o dono**, que não sabe a ocupação da agenda nem o faturamento do dia; **o profissional**, que depende de terceiros para saber seus horários; e **o cliente**, sem autonomia para marcar ou cancelar fora do horário de funcionamento.

### A proposta

Um sistema web que centraliza clientes, profissionais, serviços, horários e histórico em um só lugar — e que **impede o erro na hora de marcar**, em vez de apenas guardar dados. O diferencial do AgendaPro está nas regras de negócio: o sistema calcula quais horários estão realmente livres e recusa, com explicação, qualquer agendamento que fira uma regra.

### Perfis de usuário

| Perfil | O que faz |
| --- | --- |
| **Administrador** | Dono ou recepção. Gerencia cadastros, horários, usuários e acompanha os relatórios do negócio. |
| **Profissional** | Vê a própria agenda do dia e atualiza a situação dos atendimentos que realiza. |
| **Cliente** | Marca, consulta e cancela os próprios atendimentos, e vê o histórico do que já fez. |

---

## 2. A equipe

| Integrante | Frente de trabalho |
| --- | --- |
| João Guilherme de Mello Rodrigues | Regras do agendamento: disponibilidade, conflito, remarcação e cancelamento |
| Gabriel Victor Pais Hartmann | Banco de dados, configuração do projeto, repositório, documentação, horários e bloqueios, acesso e relatórios |
| Luiz Henrique de Morais Franco | Interface, agenda do dia e situação do atendimento |
| Thiago Gomes Oliveira | Cadastros de clientes, profissionais, serviços e histórico do cliente |

---

## 3. O que o sistema faz

### Disponível nesta versão

| Funcionalidade | Requisito | Regras aplicadas |
| --- | --- | --- |
| Cadastro, busca, edição e inativação de clientes | RF01 | RN07, RN12 |
| Cadastro, edição e inativação de serviços com duração e preço | RF04 | RN03, RN12, RN14 |
| Cadastro de profissionais e vínculo com os serviços que executam | RF03 | RN06, RN12 |
| Faixas de horário de atendimento por profissional | RF05 | RN02 |
| Bloqueios de agenda (almoço, férias, compromisso) | RF06 | RN02 |
| Consulta dos horários realmente livres | RF07 | RN01, RN02, RN03, RN04 |
| Gravação do agendamento | RF08 | RN01 a RN04, RN06, RN08, RN10 |
| Remarcação do atendimento | RF09 | RN01 a RN04, RN09 |
| Cancelamento com motivo obrigatório | RF10 | RN05, RN09 |
| Situação do atendimento com histórico de alterações | RF11 | RN09, RNF10 |
| Agenda do dia com filtros por profissional e situação | RF12 | — |
| Histórico de atendimentos do cliente | RF13 | — |
| Bloqueio automático do cliente por faltas | — | RN10 |
| Login com senha guardada como hash e sal | RF02 | RNF01 |
| Acesso por perfil: menu e telas conforme quem entrou | RF17 | RNF02 |
| Confirmação registrada ao agendar, remarcar e cancelar | RF14 | — |
| Lembrete automático 24 horas antes do atendimento | RF15 | RN11 |
| Relatório por período, profissional e serviço, com exportação em CSV | RF16 | RN13 |
| Recuperação de senha por link temporário | RF18 | — |

### O fluxo completo que já funciona

```
1. Cadastros          cliente, serviço e profissional (com os serviços que executa)
         ↓
2. Disponibilidade    faixas de horário de atendimento e bloqueios de agenda
         ↓
3. Horários livres    o sistema calcula a grade do dia e risca o que não pode
         ↓
4. Agendamento        clicar num horário livre grava o atendimento
         ↓
5. Agenda do dia      acompanhar, filtrar por profissional e por situação
         ↓
6. Atendimento        confirmar → iniciar → concluir
         ↓
7. Remarcar/cancelar  com validação de antecedência e motivo obrigatório
         ↓
8. Histórico          atendimentos do cliente e todas as mudanças registradas
```

### O que cada perfil enxerga

O acesso não é só uma tela escondida: o **mesmo** quadro de permissões monta o menu e filtra
cada requisição, na classe [`RegrasAcesso`](src/main/java/br/edu/cesumar/agendapro/dominio/regras/RegrasAcesso.java).
Digitar a URL de uma tela que não é do seu perfil leva para o aviso de acesso negado, e dois
casos de teste (CT40 e CT41) existem só para garantir que menu e filtro nunca se separem.

| Área | Administrador | Profissional | Cliente |
| --- | --- | --- | --- |
| Painel | Sim | Sim | Sim (o próprio) |
| Novo agendamento | Sim | Sim | Sim (para si) |
| Agenda do dia | Sim | Sim (a própria) | Não |
| Meus atendimentos | — | — | Sim |
| Clientes | Sim | Sim | Não |
| Profissionais, serviços | Sim | Não | Não |
| Horários e bloqueios | Sim | Sim | Não |
| Relatórios (RN13) | Sim | Não | Não |
| Notificações | Sim | Não | Não |
| Usuários | Sim | Não | Não |

### Fora do escopo do projeto

Pagamento online, integração bancária, integração com redes sociais, aplicativo para dispositivos móveis e emissão de nota fiscal.

---

## 4. Regras de negócio

As regras ficam isoladas em classes de **Java puro**, na pasta [`src/main/java/br/edu/cesumar/agendapro/dominio/regras`](src/main/java/br/edu/cesumar/agendapro/dominio/regras), sem depender do framework nem do banco. Por isso podem ser executadas e testadas sozinhas, e a mesma regra vale para qualquer tela que a utilize.

| Regra | O que garante | Onde está |
| --- | --- | --- |
| **RN01** | Nunca dois atendimentos sobrepostos para o mesmo profissional | `RegrasAgenda.conflito` |
| **RN02** | Só dentro das faixas de atendimento e fora dos bloqueios | `RegrasAgenda.dentroDoHorario` e `.bloqueio` |
| **RN03** | O término é calculado pela duração do serviço; ninguém digita | `RegrasAgenda.termino` |
| **RN04** | Não se agenda em data ou hora já passada | `RegrasAgenda.validarAgendamento` |
| **RN05** | Cliente cancela com 4 horas de antecedência; abaixo disso, só o estabelecimento | `RegrasSituacao.validarCancelamento` |
| **RN06** | Só aparece o profissional habilitado para o serviço escolhido | `Profissional.executa` |
| **RN07** | E-mail e telefone não se repetem entre clientes ativos | `RegrasCliente` |
| **RN08** | No máximo 3 agendamentos futuros ativos por cliente | `RegrasAgenda.agendamentosFuturosAtivos` |
| **RN09** | Agendado → Confirmado → Em atendimento → Concluído; situações finais não voltam atrás | `RegrasSituacao.proximas` |
| **RN10** | Cliente com 3 faltas em 90 dias fica bloqueado até o administrador liberar | `RegrasAgenda.faltasRecentes` |
| **RN11** | Um lembrete por atendimento, 24 horas antes, só para atendimento ativo | `RegrasNotificacao.deveEnviarLembrete` |
| **RN12** | Cadastros são inativados, nunca excluídos, para preservar o histórico | métodos `inativar` dos serviços |
| **RN13** | Relatório é do administrador, e o período não passa de 365 dias | `RegrasAcesso` e `RegrasRelatorio.validarPeriodo` |
| **RN14** | Não existem dois serviços ativos com o mesmo nome | `RegrasServico` |

As quatorze regras completas estão no [documento de visão e requisitos](docs/1bim-01-visao-e-requisitos.pdf).

### Como a senha é guardada (RNF01)

A senha **nunca** é gravada em texto. O que vai para o banco é o resultado de
**PBKDF2-HMAC-SHA256 com 120.000 iterações e um sal de 16 bytes sorteado por conta**, no formato
`pbkdf2$<iterações>$<sal>$<hash>`. Duas contas com a mesma senha geram registros diferentes, e a
conferência usa comparação de tempo constante.

O requisito cita o bcrypt como exemplo; a equipe optou pelo PBKDF2 porque ele já vem no próprio
JDK (`javax.crypto`), o que evita mais uma dependência externa e mantém a regra dentro de
`dominio/regras`, onde pode ser testada sozinha — como fazem os casos CT42 a CT47.

---

## 5. Tecnologias

| Camada | Tecnologia | Versão |
| --- | --- | --- |
| Interface | Thymeleaf, HTML5, CSS3 e Bootstrap | Bootstrap 5.3.3 (CDN) |
| Aplicação | Java com Spring Boot (Spring MVC e Spring Data JPA) | Java 21, Spring Boot 3.3.4 |
| Banco de dados | MySQL | 8.x |
| Build e dependências | Maven | 3.9+ |
| Testes | JUnit 5 (via `spring-boot-starter-test`) | — |

**Por que essas escolhas:** são as tecnologias que a equipe já domina, o que mantém o projeto viável dentro do prazo da disciplina. O Bootstrap entrega a grade responsiva exigida pelo RNF06 sem exigir o aprendizado de um framework de front-end, e o MySQL já era usado pela equipe e tem ferramenta gráfica de modelagem.

---

## 6. Como executar

### Requisitos

- JDK 21
- Maven 3.9 ou superior
- MySQL 8 em execução na porta 3306

### Passo a passo

```bash
# 1. clonar o repositório
git clone https://github.com/Gdzinn1200/Sabine_Atividade.git
cd Sabine_Atividade

# 2. criar o banco e as tabelas
mysql -u root -p < banco-de-dados/script_criacao.sql

# 3. carregar os dados fictícios de demonstração (recomendado)
mysql -u root -p < banco-de-dados/dados_demonstracao.sql

# 4. configurar o acesso ao banco
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
# edite o usuário e a senha do MySQL nesse arquivo

# 5. executar
mvn spring-boot:run

# 6. abrir no navegador
# http://localhost:8080
```

### Contas de demonstração

Os dados de demonstração criam quatro contas, todas com a senha **`123456`**:

| E-mail | Perfil | Serve para mostrar |
| --- | --- | --- |
| `admin@agendapro.com` | Administrador | O sistema inteiro: cadastros, relatórios, notificações e usuários |
| `ana@agendapro.com` | Profissional | A agenda limitada à própria profissional |
| `jonas@agendapro.com` | Profissional | Outro profissional, com outros serviços |
| `marina@email.com` | Cliente | O que o cliente vê: marcar e acompanhar só os próprios atendimentos |

As senhas no script são hashes PBKDF2 reais, gerados pela própria classe `Senha`: não há senha em
texto nem no repositório nem no banco.

O arquivo `application-local.properties` **não é versionado** — ele guarda a senha do banco de cada integrante. O repositório traz apenas o arquivo de exemplo, com os nomes das chaves e sem nenhum valor real.

Os dados de demonstração usam datas relativas a `CURDATE()`, então a agenda do dia aparece preenchida em qualquer data em que o script for executado. Rodar o script novamente recarrega a base do zero.

### Protótipos (sem instalar nada)

Para ver as telas sem subir a aplicação, basta abrir no navegador:

- [`prototipos/agendapro_funcional.html`](prototipos/agendapro_funcional.html) — protótipo funcional, com os dados no próprio navegador. Contas de demonstração: `admin@agendapro.com`, `ana@agendapro.com` ou `marina@email.com`, senha `123456`.
- [`prototipos/prototipo_agendapro.html`](prototipos/prototipo_agendapro.html) — protótipo de telas, com a navegação entre elas.

---

## 7. Como executar os testes

```bash
# testes automatizados das regras de negócio
mvn test

# os mesmos casos, sem depender do Maven (útil para registrar evidência rápida)
javac -d build $(find src/main/java/br/edu/cesumar/agendapro/dominio/regras -name "*.java") ferramentas/TesteRegras.java
java -cp build TesteRegras
```

**Última execução registrada: 65 casos, 65 aprovados, 0 reprovados.**
Evidência em [`docs/evidencias/execucao_testes_regras_27-11-2026.txt`](docs/evidencias/execucao_testes_regras_27-11-2026.txt).

Os casos de teste completos, incluindo os manuais, estão em [`docs/casos_de_teste_27-11-2026.md`](docs/casos_de_teste_27-11-2026.md).

Os testes encontraram dois defeitos reais antes desta entrega — um limite de período que a tela
prometia mas a regra não aplicava, e uma taxa de comparecimento sem definição escrita. Os dois
estão registrados, corrigidos e protegidos por caso de teste, na seção 3 do documento de casos de
teste.

---

## 8. Estrutura do repositório

```
├── src/                      código-fonte da aplicação
│   ├── main/java/br/edu/cesumar/agendapro/
│   │   ├── dominio/          entidades do sistema
│   │   ├── dominio/regras/   REGRAS DE NEGÓCIO em Java puro (testáveis sozinhas)
│   │   ├── repositorio/      acesso ao banco (Spring Data JPA)
│   │   ├── servico/          carrega os dados, chama as regras e grava o resultado
│   │   └── web/              controllers das telas
│   ├── main/resources/
│   │   ├── templates/        páginas Thymeleaf, separadas por assunto
│   │   └── static/css/       folha de estilo do sistema
│   └── test/java/            testes automatizados (JUnit 5)
├── ferramentas/              executor dos testes sem Maven
├── docs/                     documentos das entregas, backlog, casos de teste e evidências
├── diagramas/                casos de uso, atividades e diagrama de classes (PNG e fonte)
├── banco-de-dados/           modelos, scripts de criação e dados de demonstração
├── prototipos/               protótipos em HTML, mapa de navegação e telas
├── pom.xml                   dependências e build do projeto
└── README.md                 este arquivo
```

**Onde mexer em cada coisa:**

| Para alterar... | Mexa em... |
| --- | --- |
| uma regra de negócio | `src/main/java/.../dominio/regras` |
| uma tela | `src/main/resources/templates` |
| o que a tela recebe | `src/main/java/.../web` |
| o que é lido ou gravado | `src/main/java/.../servico` e `.../repositorio` |
| a estrutura do banco | `banco-de-dados/script_criacao.sql` |

---

## 9. Documentação das entregas

### 1º bimestre — Imersão Profissional: Projeto de Software

| # | Documento | Conteúdo |
| --- | --- | --- |
| 1 | [Visão e requisitos](docs/1bim-01-visao-e-requisitos.pdf) | Problema, objetivos, perfis, escopo, 18 requisitos funcionais, 12 não funcionais e 13 regras de negócio |
| 2 | [Modelagem do software e do banco](docs/1bim-02-modelagem-software-e-banco.pdf) | Casos de uso, diagramas de atividades, diagrama de classes, modelos conceitual e lógico, dicionário de dados e matriz de rastreabilidade |
| 3 | [GitHub, protótipos e MVP](docs/1bim-03-github-prototipos-e-mvp.pdf) | Organização do repositório, backlog, mapa de navegação, protótipos das 12 telas e definição do MVP |

### 2º bimestre — Fábrica de Software

| # | Entrega | Documento | Resultado |
| --- | --- | --- | --- |
| 1 | 20/10/2026 | [Planejamento técnico e implementação inicial](docs/2bim-01-planejamento-e-implementacao-inicial.pdf) | Código executável e primeiras evidências de testes |
| 2 | 06/11/2026 | [Implementação, integração e testes](docs/2bim-02-implementacao-integracao-e-testes.pdf) | Sistema parcialmente funcional |
| 3 | 27/11/2026 | [Conclusão e apresentação final](docs/2bim-03-conclusao-e-apresentacao-final.pdf) | Software completo e funcionando |

### Outros materiais

| Arquivo | Conteúdo |
| --- | --- |
| [`docs/backlog_2bim.csv`](docs/backlog_2bim.csv) | Backlog completo com requisito, responsável, prazo, prioridade, situação e critério de conclusão |
| [`docs/apresentacao/slides-apresentacao-final.pdf`](docs/apresentacao/slides-apresentacao-final.pdf) | Slides da apresentação final (22 slides) |
| [`docs/apresentacao/slides-banco-de-dados.pdf`](docs/apresentacao/slides-banco-de-dados.pdf) | Slides da parte de banco de dados (9 slides) |
| [`docs/casos_de_teste_27-11-2026.md`](docs/casos_de_teste_27-11-2026.md) | Casos de teste automatizados e manuais da entrega final |
| [`docs/casos_de_teste_06-11-2026.md`](docs/casos_de_teste_06-11-2026.md) | Casos de teste da entrega de 06/11 |
| [`docs/casos_de_teste_20-10-2026.md`](docs/casos_de_teste_20-10-2026.md) | Casos de teste da entrega anterior |
| [`docs/evidencias/`](docs/evidencias) | Registros de execução dos testes |

---

## 10. Rastreabilidade

Cada requisito está ligado ao lugar onde foi implementado e aos testes que o cobrem.

| Requisito | Implementação | Teste | Estado |
| --- | --- | --- | --- |
| RF01 — Manter clientes | `ClienteController`, `ClienteService`, `RegrasCliente` | CT01 a CT06, CTM01 a CTM05 | Implementado |
| RF03 — Manter profissionais | `ProfissionalController`, `ProfissionalService`, `RegrasProfissional` | CT19, CTM09, CTM10 | Implementado |
| RF04 — Manter serviços | `ServicoController`, `ServicoService`, `RegrasServico` | CT07 a CT11, CTM06, CTM07 | Implementado |
| RF05 — Horários de atendimento | `HorarioController`, `AgendaService` | CT26, CTM11, CTM12 | Implementado |
| RF06 — Bloqueios de agenda | `HorarioController`, `AgendaService` | CT16, CTM13 | Implementado |
| RF07 — Horários disponíveis | `RegrasAgenda.horariosDisponiveis` | CT17, CT18, CTM14, CTM15 | Implementado |
| RF08 — Realizar agendamento | `RegrasAgenda.validarAgendamento`, `AgendamentoService` | CT19 a CT25, CTM16 | Implementado |
| RF09 — Alterar agendamento | `AgendamentoService.remarcar` | CTM21 | Implementado |
| RF10 — Cancelar agendamento | `RegrasSituacao`, `AgendamentoService.cancelar` | CT30 a CT33, CTM22, CTM23 | Implementado |
| RF11 — Situação do atendimento | `RegrasSituacao`, `AgendamentoService` | CT27 a CT29, CTM19 | Implementado |
| RF12 — Consultar agenda | `AgendamentoController.agenda` | CTM18 | Implementado |
| RF13 — Histórico do cliente | `AgendamentoService.historicoDoCliente` | CTM25, CTM13 | Implementado |
| RF02 — Autenticar usuário | `AutenticacaoController`, `UsuarioService`, `Senha` | CT42 a CT47, CTM09, CTM10 | Implementado |
| RF14 — Notificar o cliente | `NotificacaoService`, `RegrasNotificacao` | CT55, CT56, CTM14 | Implementado |
| RF15 — Lembrete do atendimento | `NotificacaoService.enviarLembretes` | CT49 a CT54, CTM15 | Implementado |
| RF16 — Relatórios de atendimentos | `RelatorioController`, `RegrasRelatorio` | CT57 a CT62, CTM16, CTM17 | Implementado |
| RF17 — Manter usuários e perfis | `UsuarioController`, `UsuarioService` | CT34 a CT41, CTM21 | Implementado |
| RF18 — Recuperar senha | `AutenticacaoController`, `RecuperacaoSenha` | CT48, CTM19, CTM20 | Implementado |
| RNF01 — Senha com hash e sal | `dominio/regras/Senha` | CT42 a CT47 | Implementado |
| RNF02 — Acesso por perfil | `RegrasAcesso`, `SessaoFiltro`, `ContextoGlobal` | CT34 a CT41, CTM11 a CTM13 | Implementado |
| RNF05 — Mensagens de validação | `web/Mensagens` e os formulários | CT02, CT33, CTM02 | Implementado |
| RNF06 — Interface responsiva | Grade do Bootstrap e `agendapro.css` | CTM22 | Implementado |
| RNF10 — Log das operações | `HistoricoSituacao` | CTM24 | Implementado |
| RNF12 — Banco relacional | `script_criacao.sql`, repositórios JPA | CTM27 | Implementado |

---

## 11. Como a equipe trabalha

### Branches e integração

Cada tarefa é desenvolvida em uma branch própria (`feat/calculo-horarios`) e integrada à `main` por **pull request revisado por outro integrante**. Todo o código demonstrado nas entregas está na `main`, não na máquina de um integrante.

### Padrão de commits

Mensagens no formato `tipo: descrição curta no infinitivo`, citando o requisito ou a regra atendida quando aplicável.

```
feat: calcular horarios disponiveis do profissional (RF07)
feat: recusar agendamento sobreposto (RN01)
fix: liberar horario de atendimento cancelado (RN01)
docs: atualizar rastreabilidade da entrega de 06/11
```

Tipos utilizados: `feat`, `fix`, `docs`, `refactor` e `chore`.

### Tags das entregas

Cada entrega é marcada com uma tag, para identificar a versão avaliada mesmo com o desenvolvimento continuando depois:

| Tag | Entrega |
| --- | --- |
| `entrega-20-10-2026` | Planejamento técnico e implementação inicial |
| `entrega-06-11-2026` | Implementação, integração e testes |
| `entrega-27-11-2026` | Conclusão e apresentação final |

---

## 12. O que ficou pronto e o que viria depois

**Pronto:** as 34 tarefas do backlog estão concluídas e os 18 requisitos funcionais do documento
de visão estão implementados e rastreados até o teste que os cobre. O backlog completo está em
[`docs/backlog_2bim.csv`](docs/backlog_2bim.csv).

**O que um próximo ciclo trataria,** se o projeto continuasse depois da disciplina:

| Item | Por que ficou para depois |
| --- | --- |
| Envio real de e-mail (SMTP) | A regra de **quando** notificar está pronta e testada (RN11); o envio fica registrado na tela de Notificações. Ligar um servidor real mexe em um único método, `NotificacaoService.entregar`, mas exige uma conta de e-mail institucional que o projeto não tem. |
| Confirmação pelo próprio cliente por link | Hoje a confirmação é feita pelo estabelecimento. O fluxo por link reaproveitaria o mesmo mecanismo de token já usado na recuperação de senha (RF18). |
| Relatório em gráfico | Os números já saem agrupados por profissional e por serviço, e exportam em CSV. Um gráfico seria apresentação dos mesmos dados. |
| Aplicativo para celular | Fora do escopo declarado no documento de visão. As telas são responsivas e funcionam no navegador do celular (RNF06). |

---

## 13. Licença

[MIT](LICENSE). Projeto acadêmico, sem fins comerciais.
