package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CampanhaController")
class CampanhaControllerIntegrationTest extends AbstractIntegrationTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String ID_CAMPANHA_VIGENTE_SEED = "00000000-0000-0000-0000-000000000050";

    private String novaCampanhaVigenteBody(String nome) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now().plusDays(10);
        return """
                {
                  "nome": "%s",
                  "percentualDesconto": 5.00,
                  "dataInicio": "%s",
                  "dataFim": "%s",
                  "ativa": true
                }
                """.formatted(nome, FORMATTER.format(inicio), FORMATTER.format(fim));
    }

    @Test
    @DisplayName("Deve criar campanha como GERENTE")
    void deveCriarCampanha_comoGerente() throws Exception {
        String token = tokenGerente();

        mockMvc.perform(post("/api/campanhas")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novaCampanhaVigenteBody("Campanha de Teste")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.vigente").value(true));
    }

    @Test
    @DisplayName("Deve retornar 403 ao criar campanha como ATENDENTE")
    void deveRetornar403_quandoCriarCampanhaComoAtendente() throws Exception {
        String token = tokenAtendente();

        mockMvc.perform(post("/api/campanhas")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novaCampanhaVigenteBody("Campanha Negada")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar campanha com percentual de desconto inválido")
    void deveRetornar400_quandoPercentualInvalido() throws Exception {
        String token = tokenGerente();
        String body = """
                {
                  "nome": "Campanha Invalida",
                  "percentualDesconto": 150.00,
                  "dataInicio": "%s",
                  "dataFim": "%s"
                }
                """.formatted(FORMATTER.format(LocalDateTime.now()), FORMATTER.format(LocalDateTime.now().plusDays(1)));

        mockMvc.perform(post("/api/campanhas")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar campanhas vigentes, incluindo a campanha seed")
    void deveListarCampanhasVigentes() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/campanhas/vigentes")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + ID_CAMPANHA_VIGENTE_SEED + "')]").exists());
    }

    @Test
    @DisplayName("Deve atualizar campanha como GERENTE")
    void deveAtualizarCampanha_comoGerente() throws Exception {
        String token = tokenGerente();
        MvcResult criarResult = mockMvc.perform(post("/api/campanhas")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novaCampanhaVigenteBody("Campanha a Atualizar")))
                .andExpect(status().isCreated())
                .andReturn();
        String id = objectMapper.readTree(criarResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(put("/api/campanhas/" + id)
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {"ativa": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativa").value(false))
                .andExpect(jsonPath("$.vigente").value(false));
    }

    @Test
    @DisplayName("Deve excluir campanha como GERENTE")
    void deveExcluirCampanha_comoGerente() throws Exception {
        String token = tokenGerente();
        MvcResult criarResult = mockMvc.perform(post("/api/campanhas")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(novaCampanhaVigenteBody("Campanha a Excluir")))
                .andExpect(status().isCreated())
                .andReturn();
        String id = objectMapper.readTree(criarResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/api/campanhas/" + id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar campanha inexistente")
    void deveRetornar404_quandoCampanhaInexistente() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/campanhas/ffffffff-ffff-ffff-ffff-ffffffffffff")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
