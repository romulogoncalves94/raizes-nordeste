package com.projeto.raizesnordeste.presentation.records.produto;

import com.projeto.raizesnordeste.domain.enums.CategoriaProdutoEnum;
import com.projeto.raizesnordeste.domain.model.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        String nome,
        BigDecimal preco,
        CategoriaProdutoEnum categoria
) {
    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getCategoria()
        );
    }
}
