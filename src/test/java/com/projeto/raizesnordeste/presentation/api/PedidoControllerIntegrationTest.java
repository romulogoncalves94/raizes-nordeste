package com.projeto.raizesnordeste.presentation.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.projeto.raizesnordeste.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PedidoController")
class PedidoControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ID_CLIENTE_SEED = "00000000-0000-0000-0000-000000000003";
    private static final String ID_UNIDADE_SEED = "00000000-0000-0000-0000-000000000010";
    private static final String ID_CAMPANHA_VIGENTE_SEED = "00000000-0000-0000-0000-000000000050";

    private void desativarCampanhaVigenteSeed(String tokenGerente) throws Exception {
        mockMvc.perform(put("/api/campanhas/" + ID_CAMPANHA_VIGENTE_SEED)
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content("""
                                {"ativa": false}
                                """))
                .andExpect(status().isOk());
    }

    private String criarProduto(String token, String preco) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"nome": "Produto de Pedido Teste", "preco": %s, "categoria": "LANCHE"}
                                """.formatted(preco)))
                .andExpect(status().isCreated())
                .andReturn();
        return idDoBody(result);
    }

    private void criarEstoque(String token, String idUnidade, String idProduto, int quantidade) throws Exception {
        mockMvc.perform(post("/api/estoques")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": %d}
                                """.formatted(idUnidade, idProduto, quantidade)))
                .andExpect(status().isCreated());
    }

    private String idDoBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    @DisplayName("Deve criar pedido, calcular valorTotal no servidor e debitar o estoque")
    void deveCriarPedido_eDebitarEstoque() throws Exception {
        String tokenGerente = tokenGerente();
        desativarCampanhaVigenteSeed(tokenGerente);
        String idProduto = criarProduto(tokenGerente, "10.00");
        criarEstoque(tokenGerente, ID_UNIDADE_SEED, idProduto, 50);

        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "TOTEM",
                  "itens": [{"idProduto": "%s", "quantidade": 3}]
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto);

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                .andExpect(jsonPath("$.valorBruto").value(30.00))
                .andExpect(jsonPath("$.valorTotal").value(30.00))
                .andExpect(jsonPath("$.itens[0].quantidade").value(3));

        mockMvc.perform(get("/api/estoques/saldo")
                        .header("Authorization", bearer(tokenGerente))
                        .param("idUnidade", ID_UNIDADE_SEED)
                        .param("idProduto", idProduto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(47));
    }

    @Test
    @DisplayName("Deve retornar 409 ao criar pedido com estoque insuficiente")
    void deveRetornar409_quandoEstoqueInsuficiente() throws Exception {
        String tokenGerente = tokenGerente();
        desativarCampanhaVigenteSeed(tokenGerente);
        String idProduto = criarProduto(tokenGerente, "10.00");
        criarEstoque(tokenGerente, ID_UNIDADE_SEED, idProduto, 2);

        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "BALCAO",
                  "itens": [{"idProduto": "%s", "quantidade": 5}]
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto);

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve aplicar desconto de pontos resgatados sobre o valor do pedido")
    void deveAplicarDescontoDePontosResgatados() throws Exception {
        String tokenGerente = tokenGerente();
        desativarCampanhaVigenteSeed(tokenGerente);
        String idProduto = criarProduto(tokenGerente, "10.00");
        criarEstoque(tokenGerente, ID_UNIDADE_SEED, idProduto, 50);

        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "APP",
                  "itens": [{"idProduto": "%s", "quantidade": 1}],
                  "pontosResgatados": 20
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto);

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorBruto").value(10.00))
                .andExpect(jsonPath("$.valorDescontoPontos").value(0.20))
                .andExpect(jsonPath("$.valorTotal").value(9.80));
    }

    @Test
    @DisplayName("Deve retornar 409 ao resgatar mais pontos do que o saldo de fidelidade do usuário")
    void deveRetornar409_quandoSaldoDePontosInsuficiente() throws Exception {
        String tokenGerente = tokenGerente();
        desativarCampanhaVigenteSeed(tokenGerente);
        String idProduto = criarProduto(tokenGerente, "100.00");
        criarEstoque(tokenGerente, ID_UNIDADE_SEED, idProduto, 50);

        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "APP",
                  "itens": [{"idProduto": "%s", "quantidade": 1}],
                  "pontosResgatados": 999999
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto);

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve aplicar automaticamente a campanha vigente sobre o valor do pedido")
    void deveAplicarCampanhaVigenteAutomaticamente() throws Exception {
        String tokenGerente = tokenGerente();
        String idProduto = criarProduto(tokenGerente, "100.00");
        criarEstoque(tokenGerente, ID_UNIDADE_SEED, idProduto, 50);

        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "WEB",
                  "itens": [{"idProduto": "%s", "quantidade": 1}]
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto);

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCampanhaAplicada").value(ID_CAMPANHA_VIGENTE_SEED))
                .andExpect(jsonPath("$.valorDescontoCampanha").value(10.00))
                .andExpect(jsonPath("$.valorTotal").value(90.00));
    }

    @Test
    @DisplayName("Deve atualizar status para ENTREGUE e acumular pontos de fidelidade")
    void deveAtualizarStatusParaEntregue_eAcumularPontos() throws Exception {
        String tokenGerente = tokenGerente();
        desativarCampanhaVigenteSeed(tokenGerente);
        String idProduto = criarProduto(tokenGerente, "10.00");
        criarEstoque(tokenGerente, ID_UNIDADE_SEED, idProduto, 50);

        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "APP",
                  "itens": [{"idProduto": "%s", "quantidade": 1}]
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto);

        MvcResult pedidoResult = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isCreated())
                .andReturn();
        String idPedido = idDoBody(pedidoResult);

        JsonNode saldoAntes = objectMapper.readTree(mockMvc.perform(get("/api/fidelidade/" + ID_CLIENTE_SEED)
                        .header("Authorization", bearer(tokenGerente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        int pontosAntes = saldoAntes.get("saldoPontos").asInt();

        mockMvc.perform(put("/api/pedidos/" + idPedido + "/status")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content("""
                                {"status": "ENTREGUE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));

        mockMvc.perform(get("/api/fidelidade/" + ID_CLIENTE_SEED)
                        .header("Authorization", bearer(tokenGerente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoPontos").value(pontosAntes + 10));
    }

    @Test
    @DisplayName("Deve retornar 409 ao atualizar status de pedido já finalizado")
    void deveRetornar409_aoAtualizarStatusDePedidoJaFinalizado() throws Exception {
        String tokenGerente = tokenGerente();
        desativarCampanhaVigenteSeed(tokenGerente);
        String idProduto = criarProduto(tokenGerente, "10.00");
        criarEstoque(tokenGerente, ID_UNIDADE_SEED, idProduto, 50);

        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "APP",
                  "itens": [{"idProduto": "%s", "quantidade": 1}]
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto);

        MvcResult pedidoResult = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isCreated())
                .andReturn();
        String idPedido = idDoBody(pedidoResult);

        mockMvc.perform(put("/api/pedidos/" + idPedido + "/status")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content("""
                                {"status": "CANCELADO"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/pedidos/" + idPedido + "/status")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content("""
                                {"status": "COZINHA"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve cancelar pedido e estornar o estoque debitado")
    void deveCancelarPedido_eEstornarEstoque() throws Exception {
        String tokenGerente = tokenGerente();
        desativarCampanhaVigenteSeed(tokenGerente);
        String idProduto = criarProduto(tokenGerente, "10.00");
        criarEstoque(tokenGerente, ID_UNIDADE_SEED, idProduto, 50);

        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "APP",
                  "itens": [{"idProduto": "%s", "quantidade": 4}]
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto);

        MvcResult pedidoResult = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isCreated())
                .andReturn();
        String idPedido = idDoBody(pedidoResult);

        mockMvc.perform(post("/api/pedidos/" + idPedido + "/cancelar")
                        .header("Authorization", bearer(tokenGerente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        mockMvc.perform(get("/api/estoques/saldo")
                        .header("Authorization", bearer(tokenGerente))
                        .param("idUnidade", ID_UNIDADE_SEED)
                        .param("idProduto", idProduto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(50));
    }

    @Test
    @DisplayName("Deve retornar 403 ao listar pedidos como CLIENTE")
    void deveRetornar403_quandoListarPedidosComoCliente() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 401 ao criar pedido sem autenticação")
    void deveRetornar401_quandoCriarPedidoSemAutenticacao() throws Exception {
        String pedidoBody = """
                {
                  "idUsuario": "%s",
                  "idUnidade": "%s",
                  "canalPedido": "APP",
                  "itens": [{"idProduto": "00000000-0000-0000-0000-000000000020", "quantidade": 1}]
                }
                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED);

        mockMvc.perform(post("/api/pedidos")
                        .contentType("application/json")
                        .content(pedidoBody))
                .andExpect(status().isUnauthorized());
    }
}
