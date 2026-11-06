-- =====================================================================
-- AgendaPro - dados ficticios para demonstracao e testes manuais
-- Executar depois de script_criacao.sql
--
-- As datas sao calculadas a partir de CURDATE(), entao a agenda do dia
-- sempre aparece preenchida, qualquer que seja o dia da demonstracao.
--
-- As senhas gravadas aqui sao apenas exemplos. Na aplicacao o hash e
-- gerado pelo Spring Security, previsto para a entrega de 27/11 (RNF01).
-- =====================================================================

USE agendapro;

-- limpa a base de demonstracao, respeitando as chaves estrangeiras
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notificacao;
TRUNCATE TABLE historico_situacao;
TRUNCATE TABLE agendamento;
TRUNCATE TABLE bloqueio_agenda;
TRUNCATE TABLE horario_atendimento;
TRUNCATE TABLE profissional_servico;
TRUNCATE TABLE servico;
TRUNCATE TABLE profissional;
TRUNCATE TABLE cliente;
TRUNCATE TABLE usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------- usuarios
INSERT INTO usuario (id_usuario, nome, email, senha_hash, perfil) VALUES
  (1, 'Gabriel V. Hartmann', 'admin@agendapro.com', '$2a$10$exemploHashAdministrador', 'ADMINISTRADOR'),
  (2, 'Ana Paula Ribeiro',   'ana@agendapro.com',   '$2a$10$exemploHashProfissional1', 'PROFISSIONAL'),
  (3, 'Jonas Ferreira',      'jonas@agendapro.com', '$2a$10$exemploHashProfissional2', 'PROFISSIONAL'),
  (4, 'Marina Souza',        'marina@email.com',    '$2a$10$exemploHashCliente1',      'CLIENTE');

-- ------------------------------------------------------------- clientes
INSERT INTO cliente (id_cliente, id_usuario, nome, telefone, email, data_nascimento, ativo, bloqueado) VALUES
  (1, 4,    'Marina Souza',   '(42) 99876-1122', 'marina@email.com',   '1995-04-18', TRUE, FALSE),
  (2, NULL, 'Rafael Lima',    '(42) 99712-3040', 'rafael@email.com',   '1988-11-02', TRUE, FALSE),
  (3, NULL, 'Camila Reis',    '(42) 99500-7711', 'camila@email.com',   '1999-07-25', TRUE, FALSE),
  (4, NULL, 'Patricia Nunes', '(42) 99655-8890', 'patricia@email.com', '1991-02-09', TRUE, TRUE);

-- ------------------------------------------------------------- servicos
INSERT INTO servico (id_servico, nome, descricao, duracao_minutos, preco, ativo) VALUES
  (1, 'Corte feminino',  'Corte com lavagem e finalizacao', 40,  70.00, TRUE),
  (2, 'Corte masculino', 'Corte com maquina e tesoura',     30,  45.00, TRUE),
  (3, 'Coloracao',       'Coloracao completa',              90, 180.00, TRUE),
  (4, 'Barba',           'Barba com toalha quente',         25,  35.00, TRUE),
  (5, 'Hidratacao',      'Hidratacao profunda',             45,  90.00, TRUE);

-- --------------------------------------------------------- profissionais
INSERT INTO profissional (id_profissional, id_usuario, nome, telefone, email, ativo) VALUES
  (1, 2, 'Ana Paula Ribeiro', '(42) 99811-2020', 'ana@agendapro.com',   TRUE),
  (2, 3, 'Jonas Ferreira',    '(42) 99733-4455', 'jonas@agendapro.com', TRUE);

-- RN06: quais servicos cada profissional executa
INSERT INTO profissional_servico (id_profissional, id_servico) VALUES
  (1, 1), (1, 3), (1, 5),
  (2, 2), (2, 4);

-- -------------------------------------------- horarios de atendimento
-- dia_semana: 1 = domingo ... 7 = sabado
INSERT INTO horario_atendimento (id_profissional, dia_semana, hora_inicio, hora_fim) VALUES
  (1, 2, '08:00', '18:00'), (1, 3, '08:00', '18:00'), (1, 4, '08:00', '18:00'),
  (1, 5, '08:00', '18:00'), (1, 6, '08:00', '17:00'), (1, 7, '08:00', '13:00'),
  (2, 2, '09:00', '19:00'), (2, 3, '09:00', '19:00'), (2, 4, '09:00', '19:00'),
  (2, 5, '09:00', '19:00'), (2, 6, '09:00', '19:00');

-- --------------------------------------------------- bloqueios de agenda
INSERT INTO bloqueio_agenda (id_profissional, data_hora_inicio, data_hora_fim, motivo) VALUES
  (1, CONCAT(CURDATE(), ' 12:00:00'), CONCAT(CURDATE(), ' 13:00:00'), 'Almoco'),
  (2, CONCAT(DATE_ADD(CURDATE(), INTERVAL 7 DAY), ' 00:00:00'),
      CONCAT(DATE_ADD(CURDATE(), INTERVAL 7 DAY), ' 23:59:00'), 'Ferias');

-- ---------------------------------------------------------- agendamentos
-- Atendimentos de hoje, para a agenda do dia aparecer preenchida
INSERT INTO agendamento (id_agendamento, id_cliente, id_profissional, id_servico,
                         data_hora_inicio, data_hora_fim, situacao, valor, observacao, criado_em) VALUES
  (1, 1, 1, 1, CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 09:40:00'),
      'CONFIRMADO', 70.00, NULL, NOW()),
  (2, 2, 2, 4, CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 10:25:00'),
      'AGENDADO', 35.00, NULL, NOW()),
  (3, 3, 1, 3, CONCAT(CURDATE(), ' 14:00:00'), CONCAT(CURDATE(), ' 15:30:00'),
      'AGENDADO', 180.00, 'Cliente pediu tom mais claro', NOW());

-- Atendimentos ja concluidos, para o historico do cliente
INSERT INTO agendamento (id_agendamento, id_cliente, id_profissional, id_servico,
                         data_hora_inicio, data_hora_fim, situacao, valor, observacao, criado_em) VALUES
  (4, 1, 1, 1, CONCAT(DATE_SUB(CURDATE(), INTERVAL 28 DAY), ' 09:00:00'),
      CONCAT(DATE_SUB(CURDATE(), INTERVAL 28 DAY), ' 09:40:00'), 'CONCLUIDO', 70.00, NULL, NOW()),
  (5, 1, 1, 5, CONCAT(DATE_SUB(CURDATE(), INTERVAL 14 DAY), ' 10:30:00'),
      CONCAT(DATE_SUB(CURDATE(), INTERVAL 14 DAY), ' 11:15:00'), 'CONCLUIDO', 90.00, NULL, NOW()),
  (6, 2, 2, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 09:30:00'),
      CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 10:00:00'), 'CONCLUIDO', 45.00, NULL, NOW()),
  (7, 3, 1, 1, CONCAT(DATE_SUB(CURDATE(), INTERVAL 5 DAY), ' 16:00:00'),
      CONCAT(DATE_SUB(CURDATE(), INTERVAL 5 DAY), ' 16:40:00'), 'CANCELADO', 70.00, NULL, NOW());

-- RN10: tres faltas da Patricia dentro da janela de 90 dias, que justificam o bloqueio
INSERT INTO agendamento (id_agendamento, id_cliente, id_profissional, id_servico,
                         data_hora_inicio, data_hora_fim, situacao, valor, observacao, criado_em) VALUES
  (8,  4, 1, 5, CONCAT(DATE_SUB(CURDATE(), INTERVAL 10 DAY), ' 15:00:00'),
       CONCAT(DATE_SUB(CURDATE(), INTERVAL 10 DAY), ' 15:45:00'), 'NAO_COMPARECEU', 90.00, NULL, NOW()),
  (9,  4, 1, 1, CONCAT(DATE_SUB(CURDATE(), INTERVAL 35 DAY), ' 09:00:00'),
       CONCAT(DATE_SUB(CURDATE(), INTERVAL 35 DAY), ' 09:40:00'), 'NAO_COMPARECEU', 70.00, NULL, NOW()),
  (10, 4, 2, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 70 DAY), ' 11:00:00'),
       CONCAT(DATE_SUB(CURDATE(), INTERVAL 70 DAY), ' 11:30:00'), 'NAO_COMPARECEU', 45.00, NULL, NOW());

-- ----------------------------------------------- historico das situacoes
INSERT INTO historico_situacao (id_agendamento, id_usuario, situacao_anterior, situacao_nova,
                                data_hora, motivo) VALUES
  (1, 1, NULL,         'AGENDADO',       NOW(), 'Agendamento criado'),
  (1, 1, 'AGENDADO',   'CONFIRMADO',     NOW(), 'Confirmado por telefone'),
  (2, 1, NULL,         'AGENDADO',       NOW(), 'Agendamento criado'),
  (3, 1, NULL,         'AGENDADO',       NOW(), 'Agendamento criado'),
  (7, 1, 'AGENDADO',   'CANCELADO',      NOW(), 'Solicitacao do cliente'),
  (8, 1, 'CONFIRMADO', 'NAO_COMPARECEU', NOW(), 'Cliente nao compareceu');

-- ------------------------------------------------------------ conferencia
SELECT 'clientes' AS tabela, COUNT(*) AS registros FROM cliente
UNION ALL SELECT 'servicos', COUNT(*) FROM servico
UNION ALL SELECT 'profissionais', COUNT(*) FROM profissional
UNION ALL SELECT 'horarios', COUNT(*) FROM horario_atendimento
UNION ALL SELECT 'bloqueios', COUNT(*) FROM bloqueio_agenda
UNION ALL SELECT 'agendamentos', COUNT(*) FROM agendamento;
