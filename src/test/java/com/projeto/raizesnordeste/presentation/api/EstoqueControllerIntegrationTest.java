package com.projeto.raizesnordeste.presentation.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.projeto.raizesnordeste.support.AbstractIntegrationTest;
import com.projeto.raizesnordeste.support.CpfCnpjGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("EstoqueController")
class EstoqueControllerIntegrationTest extends AbstractIntegrationTest {

    private String criarUnidade(String token) throws Exception {
        String body = """
                {
                  "razaoSocial": "Unidade de Teste de Estoque",
                  "cnpj": "%s",
                  "cep": "50030230",
                  "uf": "PE"
                }
                """.formatted(CpfCnpjGenerator.randomCnpj());

        MvcResult result = mockMvc.perform(post("/api/unidades")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return idDoBody(result);
    }

    private String criarProduto(String token) throws Exception {
        String body = """
                {"nome": "Produto de Teste de Estoque", "preco": 9.90, "categoria": "LANCHE"}
                """;

        MvcResult result = mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return idDoBody(result);
    }

    private String criarEstoque(String token, String idUnidade, String idProduto, int quantidade) throws Exception {
        String body = """
                {"idUnidade": "%s", "idProduto": "%s", "quantidade": %d}
                """.formatted(idUnidade, idProduto, quantidade);

        MvcResult result = mockMvc.perform(post("/api/estoques")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return idDoBody(result);
    }

    private String idDoBody(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asText();
    }

    @Test
    @DisplayName("Deve criar estoque como GERENTE")
    void deveCriarEstoque_comoGerente() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);

        mockMvc.perform(post("/api/estoques")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": 50}
                                """.formatted(idUnidade, idProduto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidade").value(50));
    }

    @Test
    @DisplayName("Deve retornar 409 ao criar estoque duplicado para a mesma unidade/produto")
    void deveRetornar409_quandoEstoqueDuplicado() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);
        criarEstoque(token, idUnidade, idProduto, 10);

        mockMvc.perform(post("/api/estoques")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": 5}
                                """.formatted(idUnidade, idProduto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve movimentar SAIDA com quantidade suficiente e debitar o saldo")
    void deveMovimentarSaida_comQuantidadeSuficiente() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);
        criarEstoque(token, idUnidade, idProduto, 20);

        mockMvc.perform(post("/api/estoques/movimentar")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": 5, "tipo": "SAIDA"}
                                """.formatted(idUnidade, idProduto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(15));
    }

    @Test
    @DisplayName("Deve retornar 409 ao movimentar SAIDA com quantidade insuficiente")
    void deveRetornar409_quandoSaidaComQuantidadeInsuficiente() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);
        criarEstoque(token, idUnidade, idProduto, 3);

        mockMvc.perform(post("/api/estoques/movimentar")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": 10, "tipo": "SAIDA"}
                                """.formatted(idUnidade, idProduto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve movimentar ENTRADA e incrementar o saldo")
    void deveMovimentarEntrada_eIncrementarSaldo() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);
        criarEstoque(token, idUnidade, idProduto, 10);

        mockMvc.perform(post("/api/estoques/movimentar")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": 5, "tipo": "ENTRADA"}
                                """.formatted(idUnidade, idProduto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(15));
    }

    @Test
    @DisplayName("Deve retornar 404 ao movimentar estoque inexistente para a unidade/produto")
    void deveRetornar404_quandoMovimentarEstoqueInexistente() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);

        mockMvc.perform(post("/api/estoques/movimentar")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": 1, "tipo": "SAIDA"}
                                """.formatted(idUnidade, idProduto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 403 ao movimentar estoque como CLIENTE")
    void deveRetornar403_quandoMovimentarComoCliente() throws Exception {
        String tokenGerente = tokenGerente();
        String idUnidade = criarUnidade(tokenGerente);
        String idProduto = criarProduto(tokenGerente);
        criarEstoque(tokenGerente, idUnidade, idProduto, 10);

        String tokenCliente = tokenCliente();
        mockMvc.perform(post("/api/estoques/movimentar")
                        .header("Authorization", bearer(tokenCliente))
                        .contentType("application/json")
                        .content("""
                                {"idUnidade": "%s", "idProduto": "%s", "quantidade": 1, "tipo": "SAIDA"}
                                """.formatted(idUnidade, idProduto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve consultar saldo de estoque de um produto em uma unidade")
    void deveConsultarSaldo() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);
        criarEstoque(token, idUnidade, idProduto, 42);

        mockMvc.perform(get("/api/estoques/saldo")
                        .header("Authorization", bearer(token))
                        .param("idUnidade", idUnidade)
                        .param("idProduto", idProduto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(42));
    }

    @Test
    @DisplayName("Deve retornar 404 ao consultar saldo de unidade/produto sem registro de estoque")
    void deveRetornar404_quandoConsultarSaldoInexistente() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);

        mockMvc.perform(get("/api/estoques/saldo")
                        .header("Authorization", bearer(token))
                        .param("idUnidade", idUnidade)
                        .param("idProduto", idProduto))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve excluir estoque como GERENTE")
    void deveExcluirEstoque_comoGerente() throws Exception {
        String token = tokenGerente();
        String idUnidade = criarUnidade(token);
        String idProduto = criarProduto(token);
        String idEstoque = criarEstoque(token, idUnidade, idProduto, 10);

        mockMvc.perform(delete("/api/estoques/" + idEstoque)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }
}
