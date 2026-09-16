package com.projeto.raizesnordeste.presentation.records;

import com.projeto.raizesnordeste.domain.enums.CategoriaProdutoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatedProdutoRequest(
        @Schema(description = "Nome do produto", example = "Tapioca com Coco")
        @NotBlank(message = "O campo NOME não pode ser nulo ou vazio")
        @Size(min = 2, max = 150, message = "O campo NOME deve ter entre 2 e 150 caracteres.")
        String nome,

        @Schema(description = "Preço do produto", example = "18.90")
        @NotNull(message = "O campo PRECO não pode ser nulo")
        @DecimalMin(value = "0.01", message = "O campo PRECO deve ser maior que zero.")
        BigDecimal preco,

        @Schema(description = "Categoria do produto", example = "PRATO_PRINCIPAL", allowableValues = {"PRATO_PRINCIPAL", "ACOMPANHAMENTO", "BEBIDA", "SOBREMESA", "ENTRADA", "LANCHE", "OUTROS"})
        @NotNull(message = "O campo CATEGORIA não pode ser nulo")
        CategoriaProdutoEnum categoria
) {
}
