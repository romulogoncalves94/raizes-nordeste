package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PagamentoController")
class PagamentoControllerIntegrationTest extends AbstractIntegrationTest {

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

    private String criarProduto(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"nome": "Produto de Pagamento Teste", "preco": 20.00, "categoria": "LANCHE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return idDoBody(result);
    }

    private void criarEstoque(String token, String idProduto) throws Exception {
        mockMvc.perform(post("/api/estoques")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": 50}
                                """.formatted(ID_UNIDADE_SEED, idProduto)))
                .andExpect(status().isCreated());
    }

    private String criarPedido(String token, String idProduto) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {
                                  "idUsuario": "%s",
                                  "idUnidade": "%s",
                                  "canalPedido": "APP",
                                  "itens": [{"idProduto": "%s", "quantidade": 1}]
                                }
                                """.formatted(ID_CLIENTE_SEED, ID_UNIDADE_SEED, idProduto)))
                .andExpect(status().isCreated())
                .andReturn();
        return idDoBody(result);
    }

    private String idDoBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    @DisplayName("Deve aprovar pagamento e avançar o pedido para COZINHA")
    void deveAprovarPagamento_eAvancarPedidoParaCozinha() throws Exception {
        String token = tokenGerente();
        desativarCampanhaVigenteSeed(token);
        String idProduto = criarProduto(token);
        criarEstoque(token, idProduto);
        String idPedido = criarPedido(token, idProduto);

        mockMvc.perform(post("/api/pagamentos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idPedido": "%s", "formaPagamento": "PIX", "simularFalha": false}
                                """.formatted(idPedido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusPagamento").value("APROVADO"));

        mockMvc.perform(get("/api/pedidos/" + idPedido)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COZINHA"));
    }

    @Test
    @DisplayName("Deve recusar pagamento, cancelar o pedido e estornar o estoque")
    void deveRecusarPagamento_eCancelarPedido() throws Exception {
        String token = tokenGerente();
        desativarCampanhaVigenteSeed(token);
        String idProduto = criarProduto(token);
        criarEstoque(token, idProduto);
        String idPedido = criarPedido(token, idProduto);

        mockMvc.perform(post("/api/pagamentos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idPedido": "%s", "formaPagamento": "CARTAO_CREDITO", "simularFalha": true}
                                """.formatted(idPedido)))
                .andExpect(status().isPaymentRequired());

        mockMvc.perform(get("/api/pagamentos/pedido/" + idPedido)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPagamento").value("RECUSADO"));

        mockMvc.perform(get("/api/pedidos/" + idPedido)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        mockMvc.perform(get("/api/estoques/saldo")
                        .header("Authorization", bearer(token))
                        .param("idUnidade", ID_UNIDADE_SEED)
                        .param("idProduto", idProduto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(50));
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar pagar um pedido que já possui pagamento")
    void deveRetornar409_quandoPedidoJaPossuiPagamento() throws Exception {
        String token = tokenGerente();
        desativarCampanhaVigenteSeed(token);
        String idProduto = criarProduto(token);
        criarEstoque(token, idProduto);
        String idPedido = criarPedido(token, idProduto);

        mockMvc.perform(post("/api/pagamentos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idPedido": "%s", "formaPagamento": "PIX", "simularFalha": false}
                                """.formatted(idPedido)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/pagamentos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idPedido": "%s", "formaPagamento": "PIX", "simularFalha": false}
                                """.formatted(idPedido)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve retornar 404 ao pagar pedido inexistente")
    void deveRetornar404_quandoPedidoInexistente() throws Exception {
        String token = tokenGerente();

        mockMvc.perform(post("/api/pagamentos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idPedido": "ffffffff-ffff-ffff-ffff-ffffffffffff", "formaPagamento": "PIX", "simularFalha": false}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 403 ao consultar pagamento como CLIENTE")
    void deveRetornar403_quandoConsultarPagamentoComoCliente() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/pagamentos/pedido/00000000-0000-0000-0000-000000000060")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }
}
