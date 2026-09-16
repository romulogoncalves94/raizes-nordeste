package com.projeto.raizesnordeste.domain.model;

import com.projeto.raizesnordeste.domain.enums.TipoMovimentacaoEstoqueEnum;

import java.util.UUID;

public class MovimentacaoEstoque {
    private UUID idUnidade;
    private UUID idProduto;
    private Integer quantidade;
    private TipoMovimentacaoEstoqueEnum tipo;

    public MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(UUID idUnidade, UUID idProduto, Integer quantidade, TipoMovimentacaoEstoqueEnum tipo) {
        this.idUnidade = idUnidade;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.tipo = tipo;
    }

    public UUID getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(UUID idUnidade) {
        this.idUnidade = idUnidade;
    }

    public UUID getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(UUID idProduto) {
        this.idProduto = idProduto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public TipoMovimentacaoEstoqueEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacaoEstoqueEnum tipo) {
        this.tipo = tipo;
    }

}
