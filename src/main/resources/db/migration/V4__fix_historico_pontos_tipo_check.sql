-- V4__fix_historico_pontos_tipo_check.sql
-- A constraint original (V1) permitia apenas 'ACUMULO'/'RESGATE', mas o enum Java
-- TipoHistoricoPontosEnum grava 'ACUMULADO' (EnumType.STRING) — qualquer INSERT de
-- acúmulo de pontos (PedidoService.updateStatus -> ENTREGUE) violava essa constraint.

ALTER TABLE historico_pontos DROP CONSTRAINT historico_pontos_tipo_check;
ALTER TABLE historico_pontos ADD CONSTRAINT historico_pontos_tipo_check CHECK (tipo IN ('ACUMULADO', 'RESGATE'));
