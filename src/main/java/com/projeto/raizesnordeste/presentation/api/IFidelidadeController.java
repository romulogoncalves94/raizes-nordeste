package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.presentation.exceptions.StandardError;
import com.projeto.raizesnordeste.presentation.records.fidelidade.HistoricoPontosResponse;
import com.projeto.raizesnordeste.presentation.records.fidelidade.ProgramaFidelidadeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "FidelidadeController", description = "Controller responsável pelo programa de fidelidade")
@RequestMapping("/api/fidelidade")
public interface IFidelidadeController {

    @Operation(summary = "Consultar programa de fidelidade e saldo de pontos de um usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Programa encontrado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não participa do programa de fidelidade", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/{idUsuario}")
    ResponseEntity<ProgramaFidelidadeResponse> findByUsuario(
            @NotNull(message = "O id do Usuário precisa ser informado")
            @Parameter(description = "Id do Usuário", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID idUsuario
    );

    @Operation(summary = "Consultar histórico de pontos (acúmulos e resgates) de um usuário",
            description = "O resgate de pontos acontece na criação do pedido (POST /api/pedidos, campo pontosResgatados), não há endpoint de resgate avulso.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não participa do programa de fidelidade", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/{idUsuario}/historico")
    ResponseEntity<Page<HistoricoPontosResponse>> findHistorico(
            @NotNull(message = "O id do Usuário precisa ser informado")
            @Parameter(description = "Id do Usuário", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID idUsuario,

            @Parameter(description = "Número da página", example = "0", required = true)
            @RequestParam(name = "page", defaultValue = "0") final Integer page,

            @Parameter(description = "Linhas por página", example = "10", required = true)
            @RequestParam(name = "linesPerPage", defaultValue = "10") final Integer linesPerPage,

            @Parameter(description = "Ordenação", example = "DESC", required = true)
            @RequestParam(name = "direction", defaultValue = "DESC") final String direction,

            @Parameter(description = "Ordenar por atributo", example = "criadoEm", required = true)
            @RequestParam(name = "orderBy", defaultValue = "criadoEm") final String orderBy
    );
}
