package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.presentation.exceptions.StandardError;
import com.projeto.raizesnordeste.presentation.records.CreatedPagamentoRequest;
import com.projeto.raizesnordeste.presentation.records.PagamentoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "PagamentoController", description = "Controller responsável pelo pagamento (mock) dos pedidos")
@RequestMapping("/api/pagamentos")
public interface IPagamentoController {

    @Operation(summary = "Processar pagamento de um pedido (mock)",
            description = "Se aprovado, o pedido avança para COZINHA. Se recusado, o pedido é cancelado, o estoque é estornado, e a resposta é 402 Payment Required.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Pagamento aprovado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "402", description = "Pagamento recusado pelo gateway mock", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "409", description = "Pedido não está aguardando pagamento, ou já possui pagamento registrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping
    ResponseEntity<PagamentoResponse> save(@Valid @RequestBody final CreatedPagamentoRequest request);

    @Operation(summary = "Buscar pagamento por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/{id}")
    ResponseEntity<PagamentoResponse> findById(
            @NotNull(message = "O id do Pagamento precisa ser informado")
            @Parameter(description = "Id do Pagamento", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );

    @Operation(summary = "Buscar pagamento de um pedido",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado para o pedido informado", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/pedido/{idPedido}")
    ResponseEntity<PagamentoResponse> findByPedido(
            @NotNull(message = "O id do Pedido precisa ser informado")
            @Parameter(description = "Id do Pedido", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID idPedido
    );
}
