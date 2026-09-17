package com.projeto.raizesnordeste.presentation.records;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatedCampanhaRequest(
        @Schema(description = "Nome da campanha", example = "Semana do Nordeste")
        @NotBlank(message = "O campo NOME não pode ser nulo ou vazio")
        @Size(min = 3, max = 255, message = "O campo NOME deve ter entre 3 e 255 caracteres.")
        String nome,

        @Schema(description = "Percentual de desconto (0 a 100)", example = "10.00")
        @NotNull(message = "O campo percentualDesconto não pode ser nulo")
        @DecimalMin(value = "0.01", message = "O campo percentualDesconto deve ser maior que zero.")
        @DecimalMax(value = "100.00", message = "O campo percentualDesconto não pode ser maior que 100.")
        BigDecimal percentualDesconto,

        @Schema(description = "Data/hora de início da vigência", example = "2026-09-01T00:00:00")
        @NotNull(message = "O campo dataInicio não pode ser nulo")
        LocalDateTime dataInicio,

        @Schema(description = "Data/hora de fim da vigência", example = "2026-09-30T23:59:59")
        @NotNull(message = "O campo dataFim não pode ser nulo")
        LocalDateTime dataFim,

        @Schema(description = "Se a campanha está ativa (default: true)", example = "true")
        Boolean ativa
) {
}
