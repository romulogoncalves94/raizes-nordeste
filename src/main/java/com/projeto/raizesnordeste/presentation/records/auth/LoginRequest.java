package com.projeto.raizesnordeste.presentation.records.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "E-mail cadastrado", example = "fulano@exemplo.com")
        @NotBlank(message = "O campo EMAIL não pode ser nulo ou vazio")
        @Email(message = "O campo EMAIL deve ser um e-mail válido.")
        String email,

        @Schema(description = "Senha", example = "senha123")
        @NotBlank(message = "O campo SENHA não pode ser nulo ou vazio")
        String senha
) {
}
