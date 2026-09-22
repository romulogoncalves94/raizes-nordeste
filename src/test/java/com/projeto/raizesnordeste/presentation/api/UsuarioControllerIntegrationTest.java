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

@DisplayName("UsuarioController")
class UsuarioControllerIntegrationTest extends AbstractIntegrationTest {

    private String novoUsuarioBody(String cpf, String email) {
        return """
                {
                  "nome": "Usuario de Teste Integracao",
                  "cpf": "%s",
                  "email": "%s",
                  "senha": "senha123",
                  "perfil": "CLIENTE",
                  "aceiteLgpd": true,
                  "aceiteFidelidade": false
                }
                """.formatted(cpf, email);
    }

    @Test
    @DisplayName("Deve criar usuário sem autenticação (autocadastro público) e retornar 201")
    void deveCriarUsuario_semAutenticacao() throws Exception {
        String body = novoUsuarioBody(CpfCnpjGenerator.randomCpf(), "novo.usuario@raizesnordeste.com");

        mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("novo.usuario@raizesnordeste.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("Deve retornar 409 ao criar usuário com CPF já cadastrado")
    void deveRetornar409_quandoCpfDuplicado() throws Exception {
        String body = novoUsuarioBody("123.456.789-09", "outro.email@raizesnordeste.com");

        mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve retornar 409 ao criar usuário com email já cadastrado")
    void deveRetornar409_quandoEmailDuplicado() throws Exception {
        String body = novoUsuarioBody(CpfCnpjGenerator.randomCpf(), EMAIL_GERENTE_SEED);

        mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar usuário com dados inválidos")
    void deveRetornar400_quandoDadosInvalidos() throws Exception {
        String body = """
                {
                  "nome": "curto",
                  "cpf": "111",
                  "email": "invalido",
                  "senha": "123",
                  "perfil": "CLIENTE",
                  "aceiteLgpd": true,
                  "aceiteFidelidade": false
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    @DisplayName("Deve buscar usuário por id como GERENTE")
    void deveBuscarUsuarioPorId_comoGerente() throws Exception {
        String token = tokenGerente();

        mockMvc.perform(get("/api/usuarios/00000000-0000-0000-0000-000000000003")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL_CLIENTE_SEED));
    }

    @Test
    @DisplayName("Deve retornar 401 ao buscar usuário sem token")
    void deveRetornar401_quandoBuscarUsuarioSemToken() throws Exception {
        mockMvc.perform(get("/api/usuarios/00000000-0000-0000-0000-000000000003"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 403 ao buscar usuário como CLIENTE (role não permitida)")
    void deveRetornar403_quandoBuscarUsuarioComoCliente() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/usuarios/00000000-0000-0000-0000-000000000003")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar usuário inexistente")
    void deveRetornar404_quandoUsuarioInexistente() throws Exception {
        String token = tokenGerente();

        mockMvc.perform(get("/api/usuarios/ffffffff-ffff-ffff-ffff-ffffffffffff")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar usuários como ATENDENTE")
    void deveListarUsuarios_comoAtendente() throws Exception {
        String token = tokenAtendente();

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Deve atualizar o próprio usuário autenticado")
    void deveAtualizarUsuario_qualquerAutenticado() throws Exception {
        String token = tokenCliente();
        String body = """
                {"nome": "Juliana Cliente Pereira Atualizada"}
                """;

        mockMvc.perform(put("/api/usuarios/00000000-0000-0000-0000-000000000003")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Juliana Cliente Pereira Atualizada"));
    }

    @Test
    @DisplayName("Deve excluir usuário como GERENTE")
    void deveExcluirUsuario_comoGerente() throws Exception {
        String tokenGerente = tokenGerente();
        String cadastroBody = novoUsuarioBody(CpfCnpjGenerator.randomCpf(), "excluir.me@raizesnordeste.com");

        String responseBody = mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(cadastroBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(delete("/api/usuarios/" + id)
                        .header("Authorization", bearer(tokenGerente)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 403 ao excluir usuário como ATENDENTE")
    void deveRetornar403_quandoExcluirUsuarioComoAtendente() throws Exception {
        String token = tokenAtendente();

        mockMvc.perform(delete("/api/usuarios/00000000-0000-0000-0000-000000000003")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }
}
