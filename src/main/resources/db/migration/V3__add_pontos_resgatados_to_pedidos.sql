-- V3__add_pontos_resgatados_to_pedidos.sql

ALTER TABLE pedidos ADD COLUMN pontos_resgatados INT NOT NULL DEFAULT 0;
