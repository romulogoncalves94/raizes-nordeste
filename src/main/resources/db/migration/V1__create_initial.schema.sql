-- V1__create_initial_schema.sql

CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE usuarios
(
    id          UUID PRIMARY KEY             DEFAULT uuid_generate_v4(),
    nome        VARCHAR(150)        NOT NULL,
    cpf         VARCHAR(14) UNIQUE  NOT NULL,
    email       VARCHAR(150) UNIQUE NOT NULL,
    senha       VARCHAR(255)        NOT NULL,
    perfil      VARCHAR(30)         NOT NULL CHECK (perfil IN ('GERENTE', 'ATENDENTE', 'CLIENTE')),
    aceite_lgpd BOOLEAN             NOT NULL DEFAULT FALSE,
    criado_em    TIMESTAMP WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    criado_por   VARCHAR(150),
    alterado_em  TIMESTAMP WITH TIME ZONE,
    alterado_por VARCHAR(150)
);

CREATE TABLE unidades
(
    id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome     VARCHAR(150)       NOT NULL,
    cnpj     VARCHAR(18) UNIQUE NOT NULL,
    endereco VARCHAR(255)       NOT NULL,
    criado_em    TIMESTAMP WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    criado_por   VARCHAR(150),
    alterado_em  TIMESTAMP WITH TIME ZONE,
    alterado_por VARCHAR(150)
);

CREATE TABLE produtos
(
    id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome  VARCHAR(150)   NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    criado_em    TIMESTAMP WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    criado_por   VARCHAR(150),
    alterado_em  TIMESTAMP WITH TIME ZONE,
    alterado_por VARCHAR(150)
);

CREATE TABLE estoques
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_unidade UUID NOT NULL,
    id_produto UUID NOT NULL,
    quantidade INT  NOT NULL    DEFAULT 0,
    criado_em    TIMESTAMP WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    criado_por   VARCHAR(150),
    alterado_em  TIMESTAMP WITH TIME ZONE,
    alterado_por VARCHAR(150),
    CONSTRAINT fk_estoque_unidade FOREIGN KEY (id_unidade) REFERENCES unidades (id) ON DELETE CASCADE,
    CONSTRAINT fk_estoque_produto FOREIGN KEY (id_produto) REFERENCES produtos (id) ON DELETE CASCADE,
    CONSTRAINT uk_unidade_produto UNIQUE (id_unidade, id_produto)
);

CREATE TABLE pedidos
(
    id           UUID PRIMARY KEY         DEFAULT uuid_generate_v4(),
    id_usuario   UUID,
    id_unidade   UUID           NOT NULL,
    canal_pedido VARCHAR(30)    NOT NULL CHECK (canal_pedido IN ('APP', 'TOTEM', 'BALCAO', 'PICKUP', 'WEB')),
    status       VARCHAR(30)    NOT NULL CHECK (status IN ('AGUARDANDO_PAGAMENTO', 'PAGAMENTO_REALIZADO', 'RECEBIDO',
                                                           'EM_PREPARO', 'FINALIZADO', 'CANCELADO')),
    valor_total  DECIMAL(10, 2) NOT NULL,
    criado_em    TIMESTAMP WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    criado_por   VARCHAR(150),
    alterado_em  TIMESTAMP WITH TIME ZONE,
    alterado_por VARCHAR(150),
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id) ON DELETE SET NULL,
    CONSTRAINT fk_pedido_unidade FOREIGN KEY (id_unidade) REFERENCES unidades (id)
);

CREATE TABLE itens_pedido
(
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_pedido      UUID           NOT NULL,
    id_produto     UUID           NOT NULL,
    quantidade     INT            NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,
    criado_em    TIMESTAMP WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    criado_por   VARCHAR(150),
    alterado_em  TIMESTAMP WITH TIME ZONE,
    alterado_por VARCHAR(150),
    CONSTRAINT fk_item_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (id_produto) REFERENCES produtos (id)
);

CREATE TABLE pagamentos
(
    id                   UUID PRIMARY KEY         DEFAULT uuid_generate_v4(),
    id_pedido            UUID        NOT NULL,
    forma_pagamento      VARCHAR(30) NOT NULL CHECK (forma_pagamento IN
                                                     ('PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'DINHEIRO')),
    status               VARCHAR(30) NOT NULL CHECK (status IN ('PENDENTE', 'APROVADO', 'RECUSADO')),
    transacao_gateway_id VARCHAR(100),
    data_processamento   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    criado_em    TIMESTAMP WITH TIME ZONE     DEFAULT CURRENT_TIMESTAMP,
    criado_por   VARCHAR(150),
    alterado_em  TIMESTAMP WITH TIME ZONE,
    alterado_por VARCHAR(150),
    CONSTRAINT fk_pagamento_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id) ON DELETE CASCADE
);

CREATE INDEX idx_estoque_unidade_unidade ON estoque_unidade (id_unidade);
CREATE INDEX idx_estoque_unidade_produto ON estoque_unidade (id_produto);

CREATE INDEX idx_pedidos_usuario ON pedidos (id_usuario);
CREATE INDEX idx_pedidos_unidade ON pedidos (id_unidade);

CREATE INDEX idx_itens_pedido_pedido ON itens_pedido (id_pedido);
CREATE INDEX idx_itens_pedido_produto ON itens_pedido (id_produto);

CREATE INDEX idx_pagamentos_pedido ON pagamentos (id_pedido);