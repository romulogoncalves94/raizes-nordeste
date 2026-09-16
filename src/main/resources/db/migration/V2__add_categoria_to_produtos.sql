-- V2__add_categoria_to_produtos.sql

ALTER TABLE produtos ADD COLUMN categoria VARCHAR(50) NOT NULL DEFAULT 'OUTROS';

-- Atualizar constraint DEFAULT para null após adição da coluna (opcional - apenas se quiser permitir NULL depois)
-- Por agora deixamos NOT NULL com default 'OUTROS' para dados existentes
