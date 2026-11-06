-- =====================================================================
-- AgendaPro - script de criacao do banco de dados
-- Gerado a partir do modelo logico da 2a entrega (MySQL 8)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS agendapro
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE agendapro;

-- ---------------------------------------------------------------- usuario
CREATE TABLE usuario (
  id_usuario     INT           NOT NULL AUTO_INCREMENT,
  nome           VARCHAR(120)  NOT NULL,
  email          VARCHAR(120)  NOT NULL,
  senha_hash     VARCHAR(255)  NOT NULL,
  perfil         ENUM('ADMINISTRADOR','PROFISSIONAL','CLIENTE') NOT NULL,
  ativo          BOOLEAN       NOT NULL DEFAULT TRUE,
  data_cadastro  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
  CONSTRAINT uk_usuario_email UNIQUE (email)   -- RN07
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- cliente
CREATE TABLE cliente (
  id_cliente       INT          NOT NULL AUTO_INCREMENT,
  id_usuario       INT          NULL,
  nome             VARCHAR(120) NOT NULL,
  telefone         VARCHAR(20)  NOT NULL,
  email            VARCHAR(120) NULL,
  data_nascimento  DATE         NULL,
  ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
  bloqueado        BOOLEAN      NOT NULL DEFAULT FALSE,   -- RN10
  CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
  CONSTRAINT uk_cliente_email UNIQUE (email),             -- RN07
  CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
) ENGINE=InnoDB;

-- ----------------------------------------------------------- profissional
CREATE TABLE profissional (
  id_profissional  INT          NOT NULL AUTO_INCREMENT,
  id_usuario       INT          NULL,
  nome             VARCHAR(120) NOT NULL,
  telefone         VARCHAR(20)  NOT NULL,
  email            VARCHAR(120) NULL,
  ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_profissional PRIMARY KEY (id_profissional),
  CONSTRAINT fk_profissional_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- servico
CREATE TABLE servico (
  id_servico       INT           NOT NULL AUTO_INCREMENT,
  nome             VARCHAR(80)   NOT NULL,
  descricao        VARCHAR(255)  NULL,
  duracao_minutos  INT           NOT NULL,
  preco            DECIMAL(10,2) NOT NULL,
  ativo            BOOLEAN       NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_servico PRIMARY KEY (id_servico),
  CONSTRAINT ck_servico_duracao CHECK (duracao_minutos > 0),
  CONSTRAINT ck_servico_preco   CHECK (preco >= 0)
) ENGINE=InnoDB;

-- --------------------------------------------- profissional_servico (N:N)
CREATE TABLE profissional_servico (
  id_profissional  INT NOT NULL,
  id_servico       INT NOT NULL,
  CONSTRAINT pk_profissional_servico PRIMARY KEY (id_profissional, id_servico),
  CONSTRAINT fk_ps_profissional FOREIGN KEY (id_profissional)
    REFERENCES profissional (id_profissional),
  CONSTRAINT fk_ps_servico FOREIGN KEY (id_servico)
    REFERENCES servico (id_servico)
) ENGINE=InnoDB;

-- ---------------------------------------------------- horario_atendimento
CREATE TABLE horario_atendimento (
  id_horario       INT     NOT NULL AUTO_INCREMENT,
  id_profissional  INT     NOT NULL,
  dia_semana       TINYINT NOT NULL,   -- 1 = domingo ... 7 = sabado
  hora_inicio      TIME    NOT NULL,
  hora_fim         TIME    NOT NULL,
  CONSTRAINT pk_horario PRIMARY KEY (id_horario),
  CONSTRAINT fk_horario_profissional FOREIGN KEY (id_profissional)
    REFERENCES profissional (id_profissional),
  CONSTRAINT ck_horario_dia   CHECK (dia_semana BETWEEN 1 AND 7),
  CONSTRAINT ck_horario_faixa CHECK (hora_fim > hora_inicio)
) ENGINE=InnoDB;

-- -------------------------------------------------------- bloqueio_agenda
CREATE TABLE bloqueio_agenda (
  id_bloqueio       INT          NOT NULL AUTO_INCREMENT,
  id_profissional   INT          NOT NULL,
  data_hora_inicio  DATETIME     NOT NULL,
  data_hora_fim     DATETIME     NOT NULL,
  motivo            VARCHAR(120) NULL,
  CONSTRAINT pk_bloqueio PRIMARY KEY (id_bloqueio),
  CONSTRAINT fk_bloqueio_profissional FOREIGN KEY (id_profissional)
    REFERENCES profissional (id_profissional),
  CONSTRAINT ck_bloqueio_periodo CHECK (data_hora_fim > data_hora_inicio)
) ENGINE=InnoDB;

-- ------------------------------------------------------------ agendamento
CREATE TABLE agendamento (
  id_agendamento    INT           NOT NULL AUTO_INCREMENT,
  id_cliente        INT           NOT NULL,
  id_profissional   INT           NOT NULL,
  id_servico        INT           NOT NULL,
  data_hora_inicio  DATETIME      NOT NULL,
  data_hora_fim     DATETIME      NOT NULL,
  situacao          ENUM('AGENDADO','CONFIRMADO','EM_ATENDIMENTO','CONCLUIDO',
                         'CANCELADO','NAO_COMPARECEU') NOT NULL DEFAULT 'AGENDADO',
  valor             DECIMAL(10,2) NOT NULL,
  observacao        VARCHAR(255)  NULL,
  criado_em         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_agendamento PRIMARY KEY (id_agendamento),
  CONSTRAINT fk_agendamento_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente (id_cliente),
  CONSTRAINT fk_agendamento_profissional FOREIGN KEY (id_profissional)
    REFERENCES profissional (id_profissional),
  CONSTRAINT fk_agendamento_servico FOREIGN KEY (id_servico)
    REFERENCES servico (id_servico),
  CONSTRAINT ck_agendamento_periodo CHECK (data_hora_fim > data_hora_inicio)  -- RN03
) ENGINE=InnoDB;

-- apoia a verificacao de conflito (RN01) e o desempenho da agenda (RNF03)
CREATE INDEX ix_agendamento_profissional_inicio
  ON agendamento (id_profissional, data_hora_inicio);
CREATE INDEX ix_agendamento_cliente_situacao
  ON agendamento (id_cliente, situacao);

-- ------------------------------------------------------ historico_situacao
CREATE TABLE historico_situacao (
  id_historico       INT          NOT NULL AUTO_INCREMENT,
  id_agendamento     INT          NOT NULL,
  id_usuario         INT          NOT NULL,
  situacao_anterior  ENUM('AGENDADO','CONFIRMADO','EM_ATENDIMENTO','CONCLUIDO',
                          'CANCELADO','NAO_COMPARECEU') NULL,
  situacao_nova      ENUM('AGENDADO','CONFIRMADO','EM_ATENDIMENTO','CONCLUIDO',
                          'CANCELADO','NAO_COMPARECEU') NOT NULL,
  data_hora          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  motivo             VARCHAR(255) NULL,
  CONSTRAINT pk_historico PRIMARY KEY (id_historico),
  CONSTRAINT fk_historico_agendamento FOREIGN KEY (id_agendamento)
    REFERENCES agendamento (id_agendamento),
  CONSTRAINT fk_historico_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
) ENGINE=InnoDB;

-- ----------------------------------------------------------- notificacao
CREATE TABLE notificacao (
  id_notificacao    INT          NOT NULL AUTO_INCREMENT,
  id_agendamento    INT          NOT NULL,
  tipo              ENUM('CONFIRMACAO','LEMBRETE','CANCELAMENTO') NOT NULL,
  destinatario      VARCHAR(120) NOT NULL,
  data_hora_envio   DATETIME     NULL,
  enviada           BOOLEAN      NOT NULL DEFAULT FALSE,   -- RN11
  CONSTRAINT pk_notificacao PRIMARY KEY (id_notificacao),
  CONSTRAINT fk_notificacao_agendamento FOREIGN KEY (id_agendamento)
    REFERENCES agendamento (id_agendamento)
) ENGINE=InnoDB;
