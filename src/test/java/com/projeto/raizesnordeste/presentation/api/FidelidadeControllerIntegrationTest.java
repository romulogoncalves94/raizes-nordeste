package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.support.AbstractIntegrationTest;
import com.projeto.raizesnordeste.support.CpfCnpjGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("FidelidadeController")
class FidelidadeControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ID_CLIENTE_SEED = "00000000-0000-0000-0000-000000000003";

    @Test
    @DisplayName("Deve consultar saldo do programa de fidelidade do usuário seed")
    void deveConsultarSaldoDoPrograma() throws Exception {
        String token = tokenGerente();

        mockMvc.perform(get("/api/fidelidade/" + ID_CLIENTE_SEED)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(ID_CLIENTE_SEED))
                .andExpect(jsonPath("$.saldoPontos").isNumber());
    }

    @Test
    @DisplayName("Deve consultar o histórico paginado de pontos do usuário seed")
    void deveConsultarHistoricoDePontos() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/fidelidade/" + ID_CLIENTE_SEED + "/historico")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.not(org.hamcrest.Matchers.empty())));
    }

    @Test
    @DisplayName("Deve retornar 404 ao consultar fidelidade de usuário que não participa do programa")
    void deveRetornar404_quandoUsuarioNaoParticipaDoPrograma() throws Exception {
        String tokenGerente = tokenGerente();
        String cadastroBody = """
                {
                  "nome": "Usuario Sem Fidelidade Teste",
                  "cpf": "%s",
                  "email": "sem.fidelidade@raizesnordeste.com",
                  "senha": "senha123",
                  "perfil": "CLIENTE",
                  "aceiteLgpd": true,
                  "aceiteFidelidade": false
                }
                """.formatted(CpfCnpjGenerator.randomCpf());

        String responseBody = mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(cadastroBody))
                .andReturn().getResponse().getContentAsString();
        String idUsuario = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get("/api/fidelidade/" + idUsuario)
                        .header("Authorization", bearer(tokenGerente)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 401 ao consultar fidelidade sem autenticação")
    void deveRetornar401_semAutenticacao() throws Exception {
        mockMvc.perform(get("/api/fidelidade/" + ID_CLIENTE_SEED))
                .andExpect(status().isUnauthorized());
    }
}
