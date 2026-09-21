package com.projeto.raizesnordeste.presentation.records.auth;

import com.projeto.raizesnordeste.domain.enums.PerfilUsuarioEnum;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Token de acesso JWT")
        String token,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tipo,

        @Schema(description = "Perfil do usuário autenticado", example = "CLIENTE")
        PerfilUsuarioEnum perfil
) {
}
