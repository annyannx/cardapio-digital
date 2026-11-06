-- =====================================================================
-- Cardápio Digital - Esquema do banco de dados (PostgreSQL)
-- Prática Extensionista VII
--
-- Como usar (com o banco já criado):
--   psql -U postgres -d cardapio_digital -f schema.sql
-- =====================================================================

-- Para poder rodar o script mais de uma vez durante o desenvolvimento.
DROP TABLE IF EXISTS pedido_status_historico CASCADE;
DROP TABLE IF EXISTS pedido_item_adicional  CASCADE;
DROP TABLE IF EXISTS pedido_item            CASCADE;
DROP TABLE IF EXISTS pedido                 CASCADE;
DROP TABLE IF EXISTS categoria_adicional    CASCADE;
DROP TABLE IF EXISTS adicional              CASCADE;
DROP TABLE IF EXISTS item_tamanho           CASCADE;
DROP TABLE IF EXISTS item                   CASCADE;
DROP TABLE IF EXISTS categoria              CASCADE;
DROP TABLE IF EXISTS usuario                CASCADE;

-- ---------------------------------------------------------------------
-- USUÁRIOS (administradores) - RF005
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    senha_hash  VARCHAR(100) NOT NULL,              -- BCrypt, nunca a senha pura
    perfil      VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- CARDÁPIO - RF001, RF002, RF006
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id     BIGSERIAL   PRIMARY KEY,
    nome   VARCHAR(60) NOT NULL UNIQUE,             -- Pizzas, Esfihas, Refris, Sucos
    ordem  INT         NOT NULL DEFAULT 0,          -- ordem de exibição nas abas
    ativa  BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE item (
    id           BIGSERIAL    PRIMARY KEY,
    categoria_id BIGINT       NOT NULL REFERENCES categoria(id),
    codigo       VARCHAR(10),                       -- código impresso no cardápio (ex.: 201)
    nome         VARCHAR(100) NOT NULL,
    descricao    VARCHAR(255),                      -- descrição resumida (lista)
    ingredientes TEXT,                              -- detalhe (RF002)
    imagem_url   TEXT,                              -- URL do serviço de imagens
    disponivel   BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Cada item tem 1 ou mais tamanhos/variações, cada um com seu preço:
--   Pizza        -> Grande, Broto
--   Esfiha/Suco  -> Único
--   Refrigerante -> Lata, 600 ml, 1 Litro, 2 Litros
CREATE TABLE item_tamanho (
    id        BIGSERIAL     PRIMARY KEY,
    item_id   BIGINT        NOT NULL REFERENCES item(id) ON DELETE CASCADE,
    nome      VARCHAR(40)   NOT NULL,
    preco     NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    ordem     INT           NOT NULL DEFAULT 0,
    UNIQUE (item_id, nome)
);

-- Opcionais/adicionais (ex.: Catupiry, Bacon, Champignon)
CREATE TABLE adicional (
    id     BIGSERIAL     PRIMARY KEY,
    nome   VARCHAR(80)   NOT NULL UNIQUE,
    preco  NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    ativo  BOOLEAN       NOT NULL DEFAULT TRUE
);

-- Quais adicionais ficam disponíveis em cada categoria (ex.: Pizzas e Esfihas)
CREATE TABLE categoria_adicional (
    categoria_id BIGINT NOT NULL REFERENCES categoria(id) ON DELETE CASCADE,
    adicional_id BIGINT NOT NULL REFERENCES adicional(id) ON DELETE CASCADE,
    PRIMARY KEY (categoria_id, adicional_id)
);

-- ---------------------------------------------------------------------
-- PEDIDOS - RF003, RF004, RF007, RF008
-- O id do pedido é o "número do pedido" exibido ao cliente.
-- ---------------------------------------------------------------------
CREATE TABLE pedido (
    id            BIGSERIAL     PRIMARY KEY,
    cliente_nome  VARCHAR(100)  NOT NULL,
    mesa          VARCHAR(20),
    observacao    VARCHAR(255),
    status        VARCHAR(20)   NOT NULL DEFAULT 'RECEBIDO'
                  CHECK (status IN ('RECEBIDO', 'EM_PREPARO', 'PRONTO', 'ENTREGUE')),
    valor_total   NUMERIC(10,2) NOT NULL CHECK (valor_total >= 0),
    criado_em     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Os itens do pedido guardam uma "foto" (nome e preço no momento da compra).
-- Assim, se o admin mudar o preço ou excluir o item do cardápio,
-- o histórico dos pedidos continua correto.
CREATE TABLE pedido_item (
    id             BIGSERIAL     PRIMARY KEY,
    pedido_id      BIGINT        NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    item_id        BIGINT        REFERENCES item(id) ON DELETE SET NULL,
    item_nome      VARCHAR(100)  NOT NULL,
    tamanho_nome   VARCHAR(40)   NOT NULL,
    preco_unitario NUMERIC(10,2) NOT NULL CHECK (preco_unitario >= 0),
    quantidade     INT           NOT NULL CHECK (quantidade > 0),
    observacao     VARCHAR(255)
);

CREATE TABLE pedido_item_adicional (
    id             BIGSERIAL     PRIMARY KEY,
    pedido_item_id BIGINT        NOT NULL REFERENCES pedido_item(id) ON DELETE CASCADE,
    adicional_nome VARCHAR(80)   NOT NULL,
    preco          NUMERIC(10,2) NOT NULL CHECK (preco >= 0)
);

-- Histórico de mudanças de status (quem mudou e quando) - RF008
CREATE TABLE pedido_status_historico (
    id              BIGSERIAL   PRIMARY KEY,
    pedido_id       BIGINT      NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    status_anterior VARCHAR(20),                    -- nulo na criação do pedido
    status_novo     VARCHAR(20) NOT NULL,
    usuario_id      BIGINT      REFERENCES usuario(id) ON DELETE SET NULL,
    alterado_em     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- ÍNDICES
-- ---------------------------------------------------------------------
CREATE INDEX idx_item_categoria        ON item(categoria_id);
CREATE INDEX idx_item_tamanho_item     ON item_tamanho(item_id);
CREATE INDEX idx_pedido_status         ON pedido(status);
CREATE INDEX idx_pedido_criado_em      ON pedido(criado_em DESC);
CREATE INDEX idx_pedido_item_pedido    ON pedido_item(pedido_id);
CREATE INDEX idx_historico_pedido      ON pedido_status_historico(pedido_id);