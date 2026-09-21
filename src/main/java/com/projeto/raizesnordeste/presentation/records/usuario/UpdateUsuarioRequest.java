package com.projeto.raizesnordeste.presentation.records.usuario;

import com.projeto.raizesnordeste.domain.enums.PerfilUsuarioEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UpdateUsuarioRequest(
        @Schema(description = "Nome do Usuário", example = "Fulano de Tal")
        @Size(min = 10, max = 150, message = "O campo NOME deve ter entre 10 e 150 caracteres.")
        String nome,

        @Schema(description = "CPF", example = "123.456.789-00")
        @CPF(message = "O campo CPF deve ser um CPF válido.")
        @Size(min = 14, max = 14, message = "O campo CPF deve ter entre 14 caracteres.")
        String cpf,

        @Schema(description = "E-mail", example = "fulano@exemplo.com")
        @Email(message = "O campo EMAIL deve ser um e-mail válido.")
        @Size(min = 10, max = 150, message = "O campo EMAIL deve ter entre 10 e 150 caracteres.")
        String email,

        @Schema(description = "Senha", example = "senha123")
        @Size(min = 6, max = 100, message = "O campo SENHA deve ter entre 6 e 100 caracteres.")
        String senha,

        @Schema(description = "Perfil do Usuário", example = "GERENTE")
        PerfilUsuarioEnum perfil,

        @Schema(description = "Aceite da LGPD", example = "true")
        Boolean aceiteLgpd,

        @Schema(description = "Aceite da Fidelidade", example = "true")
        Boolean aceiteFidelidade
) {
}
