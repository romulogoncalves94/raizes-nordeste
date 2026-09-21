package com.projeto.raizesnordeste.presentation.records.pagamento;

import com.projeto.raizesnordeste.domain.enums.FormaPagamentoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatedPagamentoRequest(
        @Schema(description = "Id do pedido a ser pago", example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
        @NotNull(message = "O campo idPedido não pode ser nulo")
        UUID idPedido,

        @Schema(description = "Forma de pagamento", example = "PIX", allowableValues = {"PIX", "CARTAO_CREDITO", "CARTAO_DEBITO", "DINHEIRO"})
        @NotNull(message = "O campo formaPagamento não pode ser nulo")
        FormaPagamentoEnum formaPagamento,

        @Schema(description = "Uso exclusivo para testes: força o mock a recusar o pagamento", example = "false", defaultValue = "false")
        Boolean simularFalha
) {
}
