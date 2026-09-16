package com.projeto.raizesnordeste.domain.enums;

public enum CategoriaProdutoEnum {
    PRATO_PRINCIPAL("Prato Principal"),
    ACOMPANHAMENTO("Acompanhamento"),
    BEBIDA("Bebida"),
    SOBREMESA("Sobremesa"),
    ENTRADA("Entrada"),
    LANCHE("Lanche"),
    OUTROS("Outros");

    private final String descricao;

    CategoriaProdutoEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
