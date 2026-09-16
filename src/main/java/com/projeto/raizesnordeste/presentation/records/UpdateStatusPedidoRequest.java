package com.projeto.raizesnordeste.presentation.records;

import com.projeto.raizesnordeste.domain.enums.StatusPedidoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusPedidoRequest(
        @Schema(description = "Novo status do pedido", example = "COZINHA", allowableValues = {"AGUARDANDO_PAGAMENTO", "COZINHA", "PRONTO", "ENTREGUE", "CANCELADO"})
        @NotNull(message = "O campo status não pode ser nulo")
        StatusPedidoEnum status
) {
}
