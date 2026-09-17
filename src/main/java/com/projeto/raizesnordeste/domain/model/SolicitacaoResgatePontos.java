package com.projeto.raizesnordeste.domain.model;

import java.util.UUID;

public class SolicitacaoResgatePontos {
    private UUID idUsuario;
    private Integer pontos;

    public SolicitacaoResgatePontos() {
    }

    public SolicitacaoResgatePontos(UUID idUsuario, Integer pontos) {
        this.idUsuario = idUsuario;
        this.pontos = pontos;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getPontos() {
        return pontos;
    }

    public void setPontos(Integer pontos) {
        this.pontos = pontos;
    }

}
