package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController")
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Deve autenticar usuário seed e retornar token JWT")
    void deveAutenticarUsuarioSeed_eRetornarToken() throws Exception {
        String body = """
                {"email":"%s","senha":"%s"}
                """.formatted(EMAIL_GERENTE_SEED, SENHA_SEED);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.perfil").value("GERENTE"));
    }

    @Test
    @DisplayName("Deve retornar 401 ao autenticar com senha inválida")
    void deveRetornar401_quandoSenhaInvalida() throws Exception {
        String body = """
                {"email":"%s","senha":"senhaErrada"}
                """.formatted(EMAIL_GERENTE_SEED);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
    }

    @Test
    @DisplayName("Deve retornar 401 ao autenticar com e-mail inexistente")
    void deveRetornar401_quandoEmailInexistente() throws Exception {
        String body = """
                {"email":"naoexiste@raizesnordeste.com","senha":"qualquerSenha"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 400 ao autenticar com email em formato inválido")
    void deveRetornar400_quandoEmailFormatoInvalido() throws Exception {
        String body = """
                {"email":"email-invalido","senha":"qualquerSenha"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
