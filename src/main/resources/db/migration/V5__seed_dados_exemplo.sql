-- V5__seed_dados_exemplo.sql
-- Massa de dados de exemplo para testes manuais (Swagger UI) e demonstração, cobrindo
-- todas as tabelas do schema com dados variados:
--   - 3 usuários, um por perfil (GERENTE, ATENDENTE, CLIENTE) — credenciais documentadas no README;
--   - 2 unidades, 7 produtos (uma por categoria de CategoriaProdutoEnum);
--   - estoque por unidade (produto disponível em uma unidade pode não estar em outra);
--   - 3 pedidos em canais/status distintos (ENTREGUE, COZINHA, CANCELADO);
--   - pagamentos aprovado (x2) e recusado (x1), demonstrando os dois fluxos do gateway mock;
--   - programa de fidelidade com histórico de acúmulo e resgate;
--   - 3 campanhas: vigente, expirada (ativa=true fora do período) e inativa (ativa=false).
--
-- As senhas são gravadas com hash BCrypt via pgcrypto (crypt/gen_salt), compatível com o
-- BCryptPasswordEncoder do Spring Security usado pela aplicação — não há senha em texto
-- plano persistida.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ===================== USUÁRIOS (1 por perfil) =====================
-- Senha para os 3: Senha123 (ver README, seção "Usuários de teste (seed)").
INSERT INTO usuarios (id, nome, cpf, email, senha, perfil, aceite_lgpd, aceite_fidelidade)
VALUES ('00000000-0000-0000-0000-000000000001', 'Fernanda Gerente Alves', '123.456.789-09', 'gerente@raizesnordeste.com', crypt('Senha123', gen_salt('bf', 10)), 'GERENTE', true, false),
       ('00000000-0000-0000-0000-000000000002', 'Roberto Atendente Costa', '398.457.680-32', 'atendente@raizesnordeste.com', crypt('Senha123', gen_salt('bf', 10)), 'ATENDENTE', true, false),
       ('00000000-0000-0000-0000-000000000003', 'Juliana Cliente Pereira', '987.654.321-00', 'cliente@raizesnordeste.com', crypt('Senha123', gen_salt('bf', 10)), 'CLIENTE', true, true);

-- ===================== UNIDADES =====================
INSERT INTO unidades (id, razao_social, cnpj, cep, logradouro, bairro, uf)
VALUES ('00000000-0000-0000-0000-000000000010', 'Raízes do Nordeste - Centro', '60.123.456/0001-70', '50010000', 'Rua do Sol, 123', 'Centro', 'PE'),
       ('00000000-0000-0000-0000-000000000011', 'Raízes do Nordeste - Boa Viagem', '82.944.567/0001-80', '51020000', 'Av. Boa Viagem, 500', 'Boa Viagem', 'PE');

-- ===================== PRODUTOS (uma de cada categoria) =====================
INSERT INTO produtos (id, nome, preco, categoria)
VALUES ('00000000-0000-0000-0000-000000000020', 'Baião de Dois', 32.90, 'PRATO_PRINCIPAL'),
       ('00000000-0000-0000-0000-000000000021', 'Macaxeira Frita', 12.50, 'ACOMPANHAMENTO'),
       ('00000000-0000-0000-0000-000000000022', 'Suco de Caju', 8.00, 'BEBIDA'),
       ('00000000-0000-0000-0000-000000000023', 'Bolo de Rolo', 9.90, 'SOBREMESA'),
       ('00000000-0000-0000-0000-000000000024', 'Caldinho de Feijão', 7.50, 'ENTRADA'),
       ('00000000-0000-0000-0000-000000000025', 'Tapioca com Coco', 15.90, 'LANCHE'),
       ('00000000-0000-0000-0000-000000000026', 'Combo Festival Nordestino', 45.00, 'OUTROS');

-- ===================== ESTOQUES (por unidade — nem todo produto está em toda unidade) =====================
INSERT INTO estoques (id, id_unidade, id_produto, quantidade)
VALUES ('00000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000020', 40),
       ('00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000021', 25),
       ('00000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000022', 60),
       ('00000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000023', 15),
       ('00000000-0000-0000-0000-000000000034', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000024', 30),
       ('00000000-0000-0000-0000-000000000035', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000025', 20),
       ('00000000-0000-0000-0000-000000000036', '00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000026', 5),
       ('00000000-0000-0000-0000-000000000037', '00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000020', 28),
       ('00000000-0000-0000-0000-000000000038', '00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000022', 50),
       ('00000000-0000-0000-0000-000000000039', '00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000025', 18),
       ('00000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000026', 0);

-- ===================== CAMPANHAS (vigente, expirada e inativa) =====================
INSERT INTO campanhas (id, nome, percentual_desconto, data_inicio, data_fim, ativa)
VALUES ('00000000-0000-0000-0000-000000000050', 'Sexta Nordestina', 10.00, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '30 days', true),
       ('00000000-0000-0000-0000-000000000051', 'Promoção de Verão', 15.00, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '30 days', true),
       ('00000000-0000-0000-0000-000000000052', 'Campanha Desativada', 20.00, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '10 days', false);

-- ===================== PEDIDOS =====================
-- Pedido A: entregue, sem descontos — base do acúmulo de pontos de fidelidade abaixo.
-- Pedido B: em preparo (COZINHA), com desconto de pontos + campanha vigente empilhados.
-- Pedido C: cancelado, com pagamento recusado (demonstra o fluxo de recusa do gateway mock).
INSERT INTO pedidos (id, id_usuario, id_unidade, canal_pedido, status, valor_bruto, valor_desconto_pontos, valor_desconto_campanha, valor_total, pontos_resgatados, id_campanha_aplicada)
VALUES ('00000000-0000-0000-0000-000000000060', '00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000010', 'APP', 'ENTREGUE', 65.80, 0, 0, 65.80, 0, NULL),
       ('00000000-0000-0000-0000-000000000061', '00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000010', 'TOTEM', 'COZINHA', 23.90, 0.20, 2.37, 21.33, 20, '00000000-0000-0000-0000-000000000050'),
       ('00000000-0000-0000-0000-000000000062', '00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000011', 'BALCAO', 'CANCELADO', 32.90, 0, 0, 32.90, 0, NULL);

-- ===================== ITENS DOS PEDIDOS =====================
INSERT INTO itens_pedido (id, id_pedido, id_produto, quantidade, preco_unitario)
VALUES ('00000000-0000-0000-0000-000000000080', '00000000-0000-0000-0000-000000000060', '00000000-0000-0000-0000-000000000020', 2, 32.90),
       ('00000000-0000-0000-0000-000000000081', '00000000-0000-0000-0000-000000000061', '00000000-0000-0000-0000-000000000025', 1, 15.90),
       ('00000000-0000-0000-0000-000000000082', '00000000-0000-0000-0000-000000000061', '00000000-0000-0000-0000-000000000022', 1, 8.00),
       ('00000000-0000-0000-0000-000000000083', '00000000-0000-0000-0000-000000000062', '00000000-0000-0000-0000-000000000020', 1, 32.90);

-- ===================== PAGAMENTOS (2 aprovados, 1 recusado) =====================
INSERT INTO pagamentos (id, id_pedido, forma_pagamento, status_pagamento, transacao_gateway_id)
VALUES ('00000000-0000-0000-0000-000000000070', '00000000-0000-0000-0000-000000000060', 'PIX', 'APROVADO', 'SEED-TX-APROVADO-1'),
       ('00000000-0000-0000-0000-000000000071', '00000000-0000-0000-0000-000000000061', 'CARTAO_CREDITO', 'APROVADO', 'SEED-TX-APROVADO-2'),
       ('00000000-0000-0000-0000-000000000072', '00000000-0000-0000-0000-000000000062', 'DINHEIRO', 'RECUSADO', 'SEED-TX-RECUSADO-1');

-- ===================== PROGRAMA DE FIDELIDADE + HISTÓRICO =====================
-- Saldo 45 = 65 pontos acumulados (entrega do Pedido A, floor(65.80)) - 20 pontos resgatados (Pedido B).
INSERT INTO programa_fidelidade (id, id_usuario, saldo_pontos)
VALUES ('00000000-0000-0000-0000-000000000090', '00000000-0000-0000-0000-000000000003', 45);

INSERT INTO historico_pontos (id, id_fidelidade, pontos, tipo)
VALUES ('00000000-0000-0000-0000-000000000091', '00000000-0000-0000-0000-000000000090', 65, 'ACUMULADO'),
       ('00000000-0000-0000-0000-000000000092', '00000000-0000-0000-0000-000000000090', 20, 'RESGATE');
