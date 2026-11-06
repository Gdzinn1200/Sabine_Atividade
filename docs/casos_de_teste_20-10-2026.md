# Casos de teste — entrega de 20/10/2026

Projeto: **AgendaPro** — Equipe 1 — Fábrica de Software
Recorte testado: cadastro e consulta de clientes (RF01) e de serviços (RF04)
Versão testada: commit da tag `entrega-20-10-2026`
Data de execução: 20/10/2026

## 1. Testes automatizados das regras de domínio

Executados por `mvn test` (JUnit 5) e também pelo executor sem dependências
`ferramentas/TesteRegras.java`. Evidência: `docs/evidencias/execucao_testes_regras_20-10-2026.txt`.

| Caso | Requisito / regra | Condição de execução | Entrada | Resultado esperado | Resultado observado | Situação |
| --- | --- | --- | --- | --- | --- | --- |
| CT01 | RF01 | Base com 1 cliente ativo e 1 inativo | Nome, telefone e e-mail válidos | Nenhum erro; cadastro aceito | Nenhum erro | Aprovado |
| CT02 | RF01, RNF05 | Formulário vazio | Nome e telefone em branco | Dois erros: nome e telefone obrigatórios | Dois erros, nos campos indicados | Aprovado |
| CT03 | RN07 | Existe cliente ativo com `marina@email.com` | Novo cliente com o mesmo e-mail | Recusar e apontar a RN07 no campo e-mail | Erro único, regra RN07 | Aprovado |
| CT04 | RN07 | Existe cliente **inativo** com o telefone | Novo cliente com esse telefone | Aceitar: a regra vale só entre ativos | Nenhum erro | Aprovado |
| CT05 | RN07 | Editando o cliente de id 1 | Os próprios e-mail e telefone | Aceitar: o próprio registro não conflita | Nenhum erro | Aprovado |
| CT06 | RNF05 | — | E-mail `email-invalido` | Recusar com mensagem de formato | Erro no campo e-mail | Aprovado |
| CT07 | RF04 | Base com 1 serviço ativo | Serviço válido (90 min, R$ 180,00) | Nenhum erro | Nenhum erro | Aprovado |
| CT08 | RN03 | — | Duração nula e duração zero | Recusar nos dois casos citando a RN03 | Erro com regra RN03 nos dois | Aprovado |
| CT09 | RN14 | Existe serviço ativo "Corte feminino" | Nome "corte feminino" (caixa diferente) | Recusar por nome repetido | Erro único, regra RN14 | Aprovado |
| CT10 | RN03 | — | 09:00 + 90 min e 09:00 + 40 min | 10:30 e 09:40 | 10:30 e 09:40 | Aprovado |
| CT11 | RNF05 | — | Preço −1,00 | Recusar preço negativo | Erro no campo preço | Aprovado |

**Resumo:** 11 casos executados, 11 aprovados, 0 reprovados.

## 2. Testes manuais na aplicação

Executados no navegador, com a aplicação rodando em `http://localhost:8080` e o
banco `agendapro` criado pelo script `banco-de-dados/script_criacao.sql`.
Evidência: capturas em `docs/evidencias/`.

| Caso | Requisito / regra | Passos | Resultado esperado | Resultado observado | Situação |
| --- | --- | --- | --- | --- | --- |
| CTM01 | RF01 | Clientes → Novo cliente → preencher nome, telefone e e-mail → Salvar | Mensagem de sucesso e cliente na listagem | Preencher após executar | Preencher após executar |
| CTM02 | RF01, RNF05 | Novo cliente → Salvar com os campos vazios | Campos em vermelho com a mensagem de obrigatório; nada é gravado | Preencher após executar | Preencher após executar |
| CTM03 | RN07 | Cadastrar segundo cliente com e-mail já usado por um ativo | Mensagem "Já existe um cliente ativo com este e-mail (RN07)" | Preencher após executar | Preencher após executar |
| CTM04 | RF01 | Buscar pelo nome parcial na listagem | Apenas os clientes correspondentes | Preencher após executar | Preencher após executar |
| CTM05 | RN12 | Inativar um cliente e depois filtrar por "Todos" | Cliente aparece como Inativo, não é excluído | Preencher após executar | Preencher após executar |
| CTM06 | RF04, RN03 | Serviços → Novo serviço → duração 0 → Salvar | Recusa citando a RN03 | Preencher após executar | Preencher após executar |
| CTM07 | RF04 | Cadastrar serviço válido e conferir a listagem | Serviço listado com duração e preço formatados | Preencher após executar | Preencher após executar |
| CTM08 | Persistência | Parar e reiniciar a aplicação e abrir Clientes | Os registros gravados continuam disponíveis | Preencher após executar | Preencher após executar |

> As linhas de resultado observado devem ser preenchidas pela equipe no momento
> da execução, junto com a captura de tela correspondente. Um caso não executado
> não pode ser marcado como aprovado.

## 3. Defeitos encontrados

| ID | Caso de origem | Descrição | Como reproduzir | Responsável | Situação |
| --- | --- | --- | --- | --- | --- |
| — | — | Nenhum defeito registrado até o fechamento desta entrega | — | — | — |
