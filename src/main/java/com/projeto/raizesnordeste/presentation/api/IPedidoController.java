package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.presentation.exceptions.StandardError;
import com.projeto.raizesnordeste.presentation.records.pedido.CreatedPedidoRequest;
import com.projeto.raizesnordeste.presentation.records.pedido.PedidoResponse;
import com.projeto.raizesnordeste.presentation.records.pedido.UpdateStatusPedidoRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "PedidoController", description = "Controller responsável pelos pedidos multicanal")
@RequestMapping("/api/pedidos")
public interface IPedidoController {

    @Operation(summary = "Criar um novo pedido",
            description = "Calcula o valor total a partir do preço atual dos produtos e debita o estoque da unidade para cada item.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Usuário, unidade, produto ou estoque não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "409", description = "Estoque insuficiente para algum item", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping
    ResponseEntity<PedidoResponse> save(@Valid @RequestBody final CreatedPedidoRequest request);

    @Operation(summary = "Buscar pedido por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido encontrado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/{id}")
    ResponseEntity<PedidoResponse> findById(
            @NotNull(message = "O id do Pedido precisa ser informado")
            @Parameter(description = "Id do Pedido", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );

    @Operation(summary = "Buscar todos os pedidos, com filtro opcional por canal",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
            })
    @GetMapping
    ResponseEntity<Page<PedidoResponse>> findAll(
            @Parameter(description = "Número da página", example = "0", required = true)
            @RequestParam(name = "page", defaultValue = "0") final Integer page,

            @Parameter(description = "Linhas por página", example = "10", required = true)
            @RequestParam(name = "linesPerPage", defaultValue = "10") final Integer linesPerPage,

            @Parameter(description = "Ordenação", example = "ASC", required = true)
            @RequestParam(name = "direction", defaultValue = "ASC") final String direction,

            @Parameter(description = "Ordenar por atributo", example = "id", required = true)
            @RequestParam(name = "orderBy", defaultValue = "id") final String orderBy,

            @Parameter(description = "Filtrar por canal do pedido", example = "TOTEM")
            @RequestParam(name = "canalPedido", required = false) final CanalPedidoEnum canalPedido
    );

    @Operation(summary = "Atualizar o status do pedido",
            description = "Não é possível alterar um pedido já ENTREGUE ou CANCELADO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "409", description = "Pedido já finalizado (ENTREGUE/CANCELADO)", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PutMapping("/{id}/status")
    ResponseEntity<PedidoResponse> updateStatus(
            @Parameter(description = "Id do Pedido", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id,

            @Valid @RequestBody final UpdateStatusPedidoRequest request
    );

    @Operation(summary = "Cancelar pedido",
            description = "Estorna (ENTRADA) o estoque de cada item e marca o pedido como CANCELADO. Não é possível cancelar um pedido já ENTREGUE ou CANCELADO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido cancelado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "409", description = "Pedido já finalizado (ENTREGUE/CANCELADO)", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping("/{id}/cancelar")
    ResponseEntity<PedidoResponse> cancelar(
            @Parameter(description = "Id do Pedido", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );
}
