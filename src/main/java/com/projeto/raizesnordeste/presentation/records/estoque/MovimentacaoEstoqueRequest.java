package com.projeto.raizesnordeste.presentation.records.estoque;

import com.projeto.raizesnordeste.domain.enums.TipoMovimentacaoEstoqueEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record MovimentacaoEstoqueRequest(
        @Schema(description = "Id da unidade", example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
        @NotNull(message = "O campo idUnidade não pode ser nulo")
        UUID idUnidade,

        @Schema(description = "Id do produto", example = "bbb4735d-35b0-49ee-9dfb-aad6c3ff3125")
        @NotNull(message = "O campo idProduto não pode ser nulo")
        UUID idProduto,

        @Schema(description = "Quantidade a movimentar (sempre positiva)", example = "10")
        @NotNull(message = "O campo quantidade não pode ser nulo")
        @Positive(message = "O campo quantidade deve ser maior que zero")
        Integer quantidade,

        @Schema(description = "Tipo de movimentação", example = "SAIDA", allowableValues = {"ENTRADA", "SAIDA"})
        @NotNull(message = "O campo tipo não pode ser nulo")
        TipoMovimentacaoEstoqueEnum tipo
) {
}
