package com.projeto.raizesnordeste.presentation.records.estoque;

import com.projeto.raizesnordeste.domain.model.Estoque;

import java.util.UUID;

public record EstoqueResponse(
        UUID id,
        UUID idUnidade,
        String nomeUnidade,
        UUID idProduto,
        String nomeProduto,
        Integer quantidade
) {
    public static EstoqueResponse from(Estoque estoque) {
        return new EstoqueResponse(
                estoque.getId(),
                estoque.getIdUnidade(),
                estoque.getNomeUnidade(),
                estoque.getIdProduto(),
                estoque.getNomeProduto(),
                estoque.getQuantidade()
        );
    }
}
