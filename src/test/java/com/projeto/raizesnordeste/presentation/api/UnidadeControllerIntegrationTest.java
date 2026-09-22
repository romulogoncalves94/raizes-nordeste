package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.support.AbstractIntegrationTest;
import com.projeto.raizesnordeste.support.CpfCnpjGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UnidadeController")
class UnidadeControllerIntegrationTest extends AbstractIntegrationTest {

    private String novaUnidadeBody(String cnpj) {
        return """
                {
                  "razaoSocial": "Raizes do Nordeste Teste LTDA",
                  "cnpj": "%s",
                  "cep": "50030230",
                  "logradouro": "Rua de Teste, 100",
                  "bairro": "Centro",
                  "uf": "PE"
                }
                """.formatted(cnpj);
    }

    @Test
    @DisplayName("Deve criar unidade como GERENTE")
    void deveCriarUnidade_comoGerente() throws Exception {
        String token = tokenGerente();
        String body = novaUnidadeBody(CpfCnpjGenerator.randomCnpj());

        mockMvc.perform(post("/api/unidades")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.razaoSocial").value("Raizes do Nordeste Teste LTDA"));
    }

    @Test
    @DisplayName("Deve retornar 403 ao criar unidade como ATENDENTE")
    void deveRetornar403_quandoCriarUnidadeComoAtendente() throws Exception {
        String token = tokenAtendente();
        String body = novaUnidadeBody(CpfCnpjGenerator.randomCnpj());

        mockMvc.perform(post("/api/unidades")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 409 ao criar unidade com CNPJ duplicado")
    void deveRetornar409_quandoCnpjDuplicado() throws Exception {
        String token = tokenGerente();
        String body = novaUnidadeBody("60.123.456/0001-70");

        mockMvc.perform(post("/api/unidades")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve listar unidades como qualquer usuário autenticado")
    void deveListarUnidades_comoQualquerAutenticado() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/unidades")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Deve buscar unidade seed por id")
    void deveBuscarUnidadeSeedPorId() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/unidades/00000000-0000-0000-0000-000000000010")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uf").value("PE"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar unidade inexistente")
    void deveRetornar404_quandoUnidadeInexistente() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/unidades/ffffffff-ffff-ffff-ffff-ffffffffffff")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar unidade como GERENTE")
    void deveAtualizarUnidade_comoGerente() throws Exception {
        String tokenGerente = tokenGerente();
        String criarBody = novaUnidadeBody(CpfCnpjGenerator.randomCnpj());
        String responseBody = mockMvc.perform(post("/api/unidades")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(criarBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(responseBody).get("id").asText();

        String updateBody = """
                {"razaoSocial": "Raizes do Nordeste Atualizada LTDA"}
                """;

        mockMvc.perform(put("/api/unidades/" + id)
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Raizes do Nordeste Atualizada LTDA"));
    }

    @Test
    @DisplayName("Deve excluir unidade como GERENTE")
    void deveExcluirUnidade_comoGerente() throws Exception {
        String tokenGerente = tokenGerente();
        String criarBody = novaUnidadeBody(CpfCnpjGenerator.randomCnpj());
        String responseBody = mockMvc.perform(post("/api/unidades")
                        .header("Authorization", bearer(tokenGerente))
                        .contentType("application/json")
                        .content(criarBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(delete("/api/unidades/" + id)
                        .header("Authorization", bearer(tokenGerente)))
                .andExpect(status().isNoContent());
    }
}
