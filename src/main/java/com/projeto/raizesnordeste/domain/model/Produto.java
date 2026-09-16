package com.projeto.raizesnordeste.domain.model;

import com.projeto.raizesnordeste.domain.enums.CategoriaProdutoEnum;

import java.math.BigDecimal;
import java.util.UUID;

public class Produto {
    private UUID id;
    private String nome;
    private BigDecimal preco;
    private CategoriaProdutoEnum categoria;

    public Produto() {
    }

    public Produto(UUID id, String nome, BigDecimal preco, CategoriaProdutoEnum categoria) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public CategoriaProdutoEnum getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProdutoEnum categoria) {
        this.categoria = categoria;
    }

}
