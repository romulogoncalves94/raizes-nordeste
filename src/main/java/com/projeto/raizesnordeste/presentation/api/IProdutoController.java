package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.presentation.exceptions.StandardError;
import com.projeto.raizesnordeste.presentation.records.CreatedProdutoRequest;
import com.projeto.raizesnordeste.presentation.records.ProdutoResponse;
import com.projeto.raizesnordeste.presentation.records.UpdateProdutoRequest;
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

@Tag(name = "ProdutoController", description = "Controller responsável pelas operações de produtos")
@RequestMapping("/api/produtos")
public interface IProdutoController {

    @Operation(summary = "Criar um novo produto",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping
    ResponseEntity<ProdutoResponse> save(@Valid @RequestBody final CreatedProdutoRequest request);

    @Operation(summary = "Buscar produto por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/{id}")
    ResponseEntity<ProdutoResponse> findById(
            @NotNull(message = "O id do Produto precisa ser informado")
            @Parameter(description = "Id do Produto", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );

    @Operation(summary = "Buscar todos os produtos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
            })
    @GetMapping
    ResponseEntity<Page<ProdutoResponse>> findAll(
            @Parameter(description = "Número da página", example = "0", required = true)
            @RequestParam(name = "page", defaultValue = "0") final Integer page,

            @Parameter(description = "Linhas por página", example = "10", required = true)
            @RequestParam(name = "linesPerPage", defaultValue = "10") final Integer linesPerPage,

            @Parameter(description = "Ordenação", example = "ASC", required = true)
            @RequestParam(name = "direction", defaultValue = "ASC") final String direction,

            @Parameter(description = "Ordenar por atributo", example = "id", required = true)
            @RequestParam(name = "orderBy", defaultValue = "id") final String orderBy
    );

    @Operation(summary = "Atualizar produto",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PutMapping("/{id}")
    ResponseEntity<ProdutoResponse> update(
            @Parameter(description = "Id do Produto", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id,

            @Parameter(description = "Objeto do Produto para atualizar", required = true)
            @Valid @RequestBody final UpdateProdutoRequest request
    );

    @Operation(summary = "Excluir produto",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @NotNull(message = "O id do Produto precisa ser informado")
            @Parameter(description = "Id do Produto", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );
}
