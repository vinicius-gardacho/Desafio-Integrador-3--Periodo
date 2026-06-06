
CREATE DATABASE IF NOT EXISTS gestao_pedidos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gestao_pedidos;



CREATE TABLE IF NOT EXISTS clientes (
    id    INT          NOT NULL AUTO_INCREMENT,
    nome  VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,

    CONSTRAINT pk_clientes   PRIMARY KEY (id),
    CONSTRAINT uq_cli_email  UNIQUE      (email),
    CONSTRAINT ck_cli_nome   CHECK       (TRIM(nome)  <> ''),
    CONSTRAINT ck_cli_email  CHECK       (email LIKE '%_@_%._%')
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS produtos (
    id         INT              NOT NULL AUTO_INCREMENT,
    nome       VARCHAR(150)     NOT NULL,
    preco      DECIMAL(10, 2)   NOT NULL,
    estoque    INT              NOT NULL DEFAULT 0,
    categoria  ENUM(
                    'ALIMENTOS',
                    'ELETRONICOS',
                    'LIVROS'
                )                NOT NULL,

    CONSTRAINT pk_produtos      PRIMARY KEY (id),
    CONSTRAINT ck_prod_preco    CHECK       (preco   >  0),
    CONSTRAINT ck_prod_estoque  CHECK       (estoque >= 0),
    CONSTRAINT ck_prod_nome     CHECK       (TRIM(nome) <> '')
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS pedidos (
    id          INT      NOT NULL AUTO_INCREMENT,
    cliente_id  INT      NOT NULL,
    status      ENUM(
                    'ABERTO',
                    'FILA',
                    'PROCESSANDO',
                    'FINALIZADO'
                )        NOT NULL DEFAULT 'ABERTO',
    criado_em   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_pedidos         PRIMARY KEY (id),
    CONSTRAINT fk_ped_cliente     FOREIGN KEY (cliente_id)
        REFERENCES clientes (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS itens_pedido (
    id          INT            NOT NULL AUTO_INCREMENT,
    pedido_id   INT            NOT NULL,
    produto_id  INT            NOT NULL,
    quantidade  INT            NOT NULL,
    preco_unit  DECIMAL(10, 2) NOT NULL,  

    CONSTRAINT pk_itens           PRIMARY KEY (id),
    CONSTRAINT uq_item_ped_prod   UNIQUE      (pedido_id, produto_id),
    CONSTRAINT fk_item_pedido     FOREIGN KEY (pedido_id)
        REFERENCES pedidos  (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_item_produto    FOREIGN KEY (produto_id)
        REFERENCES produtos (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT ck_item_qtd        CHECK (quantidade > 0),
    CONSTRAINT ck_item_preco_unit CHECK (preco_unit > 0)
) ENGINE = InnoDB;

INSERT INTO clientes (nome, email) VALUES
    ('Ana Souza',    'ana.souza@email.com'),
    ('Bruno Lima',   'bruno.lima@email.com'),
    ('Carla Mendes', 'carla.mendes@email.com');

INSERT INTO produtos (nome, preco, estoque, categoria) VALUES
    ('Arroz Integral 1kg',    8.90,  50, 'ALIMENTOS'),
    ('Notebook Gamer 16GB',   4599.00, 5, 'ELETRONICOS'),
    ('Clean Code – R. Martin', 89.90, 20, 'LIVROS'),
    ('Feijão Carioca 1kg',    7.50,  40, 'ALIMENTOS'),
    ('Fone Bluetooth JBL',    299.90, 15, 'ELETRONICOS');
