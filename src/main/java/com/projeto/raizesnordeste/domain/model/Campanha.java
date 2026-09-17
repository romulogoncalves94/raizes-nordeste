package com.projeto.raizesnordeste.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Campanha {
    private UUID id;
    private String nome;
    private BigDecimal percentualDesconto;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Boolean ativa;

    public Campanha() {
    }

    public Campanha(UUID id, String nome, BigDecimal percentualDesconto, LocalDateTime dataInicio, LocalDateTime dataFim, Boolean ativa) {
        this.id = id;
        this.nome = nome;
        this.percentualDesconto = percentualDesconto;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.ativa = ativa;
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

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setPercentualDesconto(BigDecimal percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

}
