package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.presentation.exceptions.StandardError;
import com.projeto.raizesnordeste.presentation.records.estoque.CreatedEstoqueRequest;
import com.projeto.raizesnordeste.presentation.records.estoque.EstoqueResponse;
import com.projeto.raizesnordeste.presentation.records.estoque.MovimentacaoEstoqueRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "EstoqueController", description = "Controller responsável pelo controle de estoque por unidade")
@RequestMapping("/api/estoques")
public interface IEstoqueController {

    @Operation(summary = "Criar registro de estoque para uma unidade/produto",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Estoque criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Unidade ou produto não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "409", description = "Já existe registro de estoque para esta unidade/produto", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping
    ResponseEntity<EstoqueResponse> save(@Valid @RequestBody final CreatedEstoqueRequest request);

    @Operation(summary = "Buscar estoque por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estoque encontrado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Estoque não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/{id}")
    ResponseEntity<EstoqueResponse> findById(
            @NotNull(message = "O id do Estoque precisa ser informado")
            @Parameter(description = "Id do Estoque", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );

    @Operation(summary = "Buscar todos os registros de estoque",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de estoques retornada com sucesso")
            })
    @GetMapping
    ResponseEntity<Page<EstoqueResponse>> findAll(
            @Parameter(description = "Número da página", example = "0", required = true)
            @RequestParam(name = "page", defaultValue = "0") final Integer page,

            @Parameter(description = "Linhas por página", example = "10", required = true)
            @RequestParam(name = "linesPerPage", defaultValue = "10") final Integer linesPerPage,

            @Parameter(description = "Ordenação", example = "ASC", required = true)
            @RequestParam(name = "direction", defaultValue = "ASC") final String direction,

            @Parameter(description = "Ordenar por atributo", example = "id", required = true)
            @RequestParam(name = "orderBy", defaultValue = "id") final String orderBy
    );

    @Operation(summary = "Consultar saldo de estoque de um produto em uma unidade",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Saldo consultado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Estoque não encontrado para a unidade/produto informados", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/saldo")
    ResponseEntity<EstoqueResponse> findByUnidadeAndProduto(
            @Parameter(description = "Id da unidade", required = true)
            @RequestParam("idUnidade") UUID idUnidade,

            @Parameter(description = "Id do produto", required = true)
            @RequestParam("idProduto") UUID idProduto
    );

    @Operation(summary = "Movimentar estoque (entrada ou saída)",
            description = "Fluxo crítico do MVP: valida disponibilidade em caso de saída, atualiza a quantidade de forma atômica (lock pessimista) e registra auditoria (usuário/data da alteração).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Movimentação realizada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Estoque não encontrado para a unidade/produto informados", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "409", description = "Quantidade insuficiente em estoque para saída", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping("/movimentar")
    ResponseEntity<EstoqueResponse> movimentar(@Valid @RequestBody final MovimentacaoEstoqueRequest request);

    @Operation(summary = "Excluir registro de estoque",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Estoque removido com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Estoque não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @NotNull(message = "O id do Estoque precisa ser informado")
            @Parameter(description = "Id do Estoque", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );
}
