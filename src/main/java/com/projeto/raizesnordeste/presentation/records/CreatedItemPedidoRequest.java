package com.projeto.raizesnordeste.presentation.records;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreatedItemPedidoRequest(
        @Schema(description = "Id do produto", example = "bbb4735d-35b0-49ee-9dfb-aad6c3ff3125")
        @NotNull(message = "O campo idProduto não pode ser nulo")
        UUID idProduto,

        @Schema(description = "Quantidade do produto no pedido", example = "2")
        @NotNull(message = "O campo quantidade não pode ser nulo")
        @Positive(message = "O campo quantidade deve ser maior que zero")
        Integer quantidade
) {
}
