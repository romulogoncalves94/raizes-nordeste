package com.projeto.raizesnordeste.domain.model;

import java.util.UUID;

public class Estoque {
    private UUID id;
    private UUID idUnidade;
    private String nomeUnidade;
    private UUID idProduto;
    private String nomeProduto;
    private Integer quantidade;

    public Estoque() {
    }

    public Estoque(UUID id, UUID idUnidade, String nomeUnidade, UUID idProduto, String nomeProduto, Integer quantidade) {
        this.id = id;
        this.idUnidade = idUnidade;
        this.nomeUnidade = nomeUnidade;
        this.idProduto = idProduto;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(UUID idUnidade) {
        this.idUnidade = idUnidade;
    }

    public String getNomeUnidade() {
        return nomeUnidade;
    }

    public void setNomeUnidade(String nomeUnidade) {
        this.nomeUnidade = nomeUnidade;
    }

    public UUID getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(UUID idProduto) {
        this.idProduto = idProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

}
