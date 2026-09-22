package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ProdutoController")
class ProdutoControllerIntegrationTest extends AbstractIntegrationTest {

    private String novoProdutoBody(String nome, String preco) {
        return """
                {"nome": "%s", "preco": %s, "categoria": "LANCHE"}
                """.formatted(nome, preco);
    }

    @Test
    @DisplayName("Deve criar produto como GERENTE")
    void deveCriarProduto_comoGerente() throws Exception {
        String token = tokenGerente();

        mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novoProdutoBody("Cuscuz com Ovo", "14.90")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Cuscuz com Ovo"));
    }

    @Test
    @DisplayName("Deve criar produto como ATENDENTE")
    void deveCriarProduto_comoAtendente() throws Exception {
        String token = tokenAtendente();

        mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novoProdutoBody("Suco de Acerola", "7.00")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve retornar 403 ao criar produto como CLIENTE")
    void deveRetornar403_quandoCriarProdutoComoCliente() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novoProdutoBody("Suco de Acerola", "7.00")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar produto com preço inválido")
    void deveRetornar400_quandoPrecoInvalido() throws Exception {
        String token = tokenGerente();

        mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novoProdutoBody("Produto Invalido", "0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve buscar produto seed por id")
    void deveBuscarProdutoSeedPorId() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/produtos/00000000-0000-0000-0000-000000000020")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Baião de Dois"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar produto inexistente")
    void deveRetornar404_quandoProdutoInexistente() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/produtos/ffffffff-ffff-ffff-ffff-ffffffffffff")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar produto como ATENDENTE")
    void deveAtualizarProduto_comoAtendente() throws Exception {
        String tokenGerente = tokenGerente();
        String responseBody = mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(novoProdutoBody("Produto a Atualizar", "10.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(responseBody).get("id").asText();

        String tokenAtendente = tokenAtendente();
        mockMvc.perform(put("/api/produtos/" + id)
                        .header("Authorization", bearer(tokenAtendente))
                        .contentType("application/json")
                        .content("""
                                {"preco": 12.50}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(12.50));
    }

    @Test
    @DisplayName("Deve excluir produto como GERENTE")
    void deveExcluirProduto_comoGerente() throws Exception {
        String token = tokenGerente();
        String responseBody = mockMvc.perform(post("/api/produtos")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novoProdutoBody("Produto a Excluir", "5.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(delete("/api/produtos/" + id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }
}
