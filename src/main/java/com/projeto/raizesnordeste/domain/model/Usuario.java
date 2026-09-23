package com.projeto.raizesnordeste.domain.model;

import com.projeto.raizesnordeste.domain.enums.PerfilUsuarioEnum;

import java.util.UUID;

public class Usuario {
    private UUID id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private PerfilUsuarioEnum perfil;
    private Boolean aceiteLgpd;
    private Boolean aceiteFidelidade;

    public Usuario() {
    }

    public Usuario(UUID id, String nome, String cpf, String email, String senha, PerfilUsuarioEnum perfil, Boolean aceiteLgpd, Boolean aceiteFidelidade) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.aceiteLgpd = aceiteLgpd;
        this.aceiteFidelidade = aceiteFidelidade;
    }

    public Usuario(UUID id, String nome) {
        this.id = id;
        this.nome = nome;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public PerfilUsuarioEnum getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuarioEnum perfil) {
        this.perfil = perfil;
    }

    public Boolean getAceiteLgpd() {
        return aceiteLgpd;
    }

    public void setAceiteLgpd(Boolean aceiteLgpd) {
        this.aceiteLgpd = aceiteLgpd;
    }

    public Boolean getAceiteFidelidade() {
        return aceiteFidelidade;
    }

    public void setAceiteFidelidade(Boolean aceiteFidelidade) {
        this.aceiteFidelidade = aceiteFidelidade;
    }

}
