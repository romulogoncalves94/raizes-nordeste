package com.projeto.raizesnordeste.domain.model;

import java.util.UUID;

public class ProgramaFidelidade {
    private UUID id;
    private UUID idUsuario;
    private String nomeUsuario;
    private Integer saldoPontos;

    public ProgramaFidelidade() {
    }

    public ProgramaFidelidade(UUID id, UUID idUsuario, String nomeUsuario, Integer saldoPontos) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.saldoPontos = saldoPontos;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public Integer getSaldoPontos() {
        return saldoPontos;
    }

    public void setSaldoPontos(Integer saldoPontos) {
        this.saldoPontos = saldoPontos;
    }

}
