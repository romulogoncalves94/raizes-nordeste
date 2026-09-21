package com.projeto.raizesnordeste.presentation.records.pedido;

import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.UUID;

public record CreatedPedidoRequest(
        @Schema(description = "Id do usuário/cliente do pedido", example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
        @NotNull(message = "O campo idUsuario não pode ser nulo")
        UUID idUsuario,

        @Schema(description = "Id da unidade onde o pedido foi realizado", example = "ccc4735d-35b0-49ee-9dfb-aad6c3ff3125")
        @NotNull(message = "O campo idUnidade não pode ser nulo")
        UUID idUnidade,

        @Schema(description = "Canal do pedido", example = "TOTEM", allowableValues = {"APP", "TOTEM", "BALCAO", "PICKUP", "WEB"})
        @NotNull(message = "O campo canalPedido não pode ser nulo")
        CanalPedidoEnum canalPedido,

        @Schema(description = "Itens do pedido")
        @NotEmpty(message = "O pedido precisa ter ao menos um item")
        @Valid
        List<CreatedItemPedidoRequest> itens,

        @Schema(description = "Pontos de fidelidade a resgatar como desconto (100 pontos = R$1,00). Opcional.", example = "0")
        @PositiveOrZero(message = "O campo pontosResgatados não pode ser negativo")
        Integer pontosResgatados
) {
}
