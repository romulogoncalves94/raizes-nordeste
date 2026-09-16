package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.presentation.exceptions.StandardError;
import com.projeto.raizesnordeste.presentation.records.CreatedUnidadeRequest;
import com.projeto.raizesnordeste.presentation.records.UnidadeResponse;
import com.projeto.raizesnordeste.presentation.records.UpdateUnidadeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "UnidadeController", description = "Controller responsável pelas operações de unidades")
@RequestMapping("/api/unidades")
public interface IUnidadeController {

    @Operation(summary = "Criar uma nova unidade",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Unidade criada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping
    ResponseEntity<UnidadeResponse> save(@Valid @RequestBody final CreatedUnidadeRequest request);

    @Operation(summary = "Buscar unidade por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unidade encontrada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Unidade não encontrada", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/{id}")
    ResponseEntity<UnidadeResponse> findById(
            @NotNull(message = "O id da Unidade precisa ser informado")
            @Parameter(description = "Id da Unidade", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );

    @Operation(summary = "Buscar todas as unidades",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de unidades retornada com sucesso")
            })
    @GetMapping
    ResponseEntity<Page<UnidadeResponse>> findAll(
            @Parameter(description = "Número da página", example = "0", required = true)
            @RequestParam(name = "page", defaultValue = "0") final Integer page,

            @Parameter(description = "Linhas por página", example = "10", required = true)
            @RequestParam(name = "linesPerPage", defaultValue = "10") final Integer linesPerPage,

            @Parameter(description = "Ordenação", example = "ASC", required = true)
            @RequestParam(name = "direction", defaultValue = "ASC") final String direction,

            @Parameter(description = "Ordenar por atributo", example = "id", required = true)
            @RequestParam(name = "orderBy", defaultValue = "id") final String orderBy
    );

    @Operation(summary = "Atualizar unidade",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unidade atualizada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Unidade não encontrada", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PutMapping("/{id}")
    ResponseEntity<UnidadeResponse> update(
            @Parameter(description = "Id da Unidade", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id,

            @Parameter(description = "Objeto da Unidade para atualizar", required = true)
            @Valid @RequestBody final UpdateUnidadeRequest request
    );

    @Operation(summary = "Excluir unidade",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Unidade removida com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Unidade não encontrada", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @NotNull(message = "O id da Unidade precisa ser informado")
            @Parameter(description = "Id da Unidade", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );
}
