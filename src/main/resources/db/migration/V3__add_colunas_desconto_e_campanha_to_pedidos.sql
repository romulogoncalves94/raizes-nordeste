-- V3__add_colunas_desconto_e_campanha_to_pedidos.sql

ALTER TABLE pedidos
    ADD COLUMN pontos_resgatados INT NOT NULL DEFAULT 0,
    ADD COLUMN valor_bruto DECIMAL(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN valor_desconto_pontos DECIMAL(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN id_campanha_aplicada UUID NULL,
    ADD COLUMN valor_desconto_campanha DECIMAL(10, 2) NOT NULL DEFAULT 0,
    ADD CONSTRAINT fk_pedido_campanha FOREIGN KEY (id_campanha_aplicada) REFERENCES campanhas (id);