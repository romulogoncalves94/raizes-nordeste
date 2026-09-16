package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.presentation.exceptions.StandardError;
import com.projeto.raizesnordeste.presentation.records.LoginRequest;
import com.projeto.raizesnordeste.presentation.records.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "AuthController", description = "Controller responsável pela autenticação")
@RequestMapping("/api/auth")
public interface IAuthController {

    @Operation(summary = "Autenticar usuário e obter token JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request);

}
