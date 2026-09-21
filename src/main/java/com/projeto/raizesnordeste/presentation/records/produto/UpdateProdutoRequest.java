package com.projeto.raizesnordeste.presentation.records.produto;

import com.projeto.raizesnordeste.domain.enums.CategoriaProdutoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateProdutoRequest(
        @Schema(description = "Nome do produto", example = "Tapioca com Coco")
        String nome,

        @Schema(description = "Preço do produto", example = "18.90")
        @DecimalMin(value = "0.01", message = "O campo PRECO deve ser maior que zero.")
        BigDecimal preco,

        @Schema(description = "Categoria do produto", example = "PRATO_PRINCIPAL", allowableValues = {"PRATO_PRINCIPAL", "ACOMPANHAMENTO", "BEBIDA", "SOBREMESA", "ENTRADA", "LANCHE", "OUTROS"})
        CategoriaProdutoEnum categoria
) {
}
