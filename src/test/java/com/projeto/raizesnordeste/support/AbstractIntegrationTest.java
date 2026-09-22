package com.projeto.raizesnordeste.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base para os testes de integração dos controllers REST: sobe um Postgres real via
 * Testcontainers (as migrations do Flyway, incluindo o seed V5, rodam normalmente),
 * expõe MockMvc/ObjectMapper e helpers de autenticação. O container é um singleton
 * estático reaproveitado por todas as subclasses (mais rápido que um por classe).
 * Cada método de teste roda dentro de uma transação que é revertida ao final, então
 * mutações feitas por um teste não vazam para os demais.
 */
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("raizes_db_test")
            .withUsername("raizes_user")
            .withPassword("raizes_password");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    public static final String SENHA_SEED = "Senha123";
    public static final String EMAIL_GERENTE_SEED = "gerente@raizesnordeste.com";
    public static final String EMAIL_ATENDENTE_SEED = "atendente@raizesnordeste.com";
    public static final String EMAIL_CLIENTE_SEED = "cliente@raizesnordeste.com";

    protected String loginEObterToken(String email, String senha) throws Exception {
        String body = """
                {"email":"%s","senha":"%s"}
                """.formatted(email, senha);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    protected String tokenGerente() throws Exception {
        return loginEObterToken(EMAIL_GERENTE_SEED, SENHA_SEED);
    }

    protected String tokenAtendente() throws Exception {
        return loginEObterToken(EMAIL_ATENDENTE_SEED, SENHA_SEED);
    }

    protected String tokenCliente() throws Exception {
        return loginEObterToken(EMAIL_CLIENTE_SEED, SENHA_SEED);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
