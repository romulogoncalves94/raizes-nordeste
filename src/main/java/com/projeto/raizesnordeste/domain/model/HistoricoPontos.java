package com.projeto.raizesnordeste.domain.model;

import com.projeto.raizesnordeste.domain.enums.TipoHistoricoPontosEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public class HistoricoPontos {
    private UUID id;
    private UUID idProgramaFidelidade;
    private Integer pontos;
    private TipoHistoricoPontosEnum tipoHistorico;
    private LocalDateTime registradoEm;

    public HistoricoPontos() {
    }

    public HistoricoPontos(UUID id, UUID idProgramaFidelidade, Integer pontos, TipoHistoricoPontosEnum tipoHistorico, LocalDateTime registradoEm) {
        this.id = id;
        this.idProgramaFidelidade = idProgramaFidelidade;
        this.pontos = pontos;
        this.tipoHistorico = tipoHistorico;
        this.registradoEm = registradoEm;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdProgramaFidelidade() {
        return idProgramaFidelidade;
    }

    public void setIdProgramaFidelidade(UUID idProgramaFidelidade) {
        this.idProgramaFidelidade = idProgramaFidelidade;
    }

    public Integer getPontos() {
        return pontos;
    }

    public void setPontos(Integer pontos) {
        this.pontos = pontos;
    }

    public TipoHistoricoPontosEnum getTipoHistorico() {
        return tipoHistorico;
    }

    public void setTipoHistorico(TipoHistoricoPontosEnum tipoHistorico) {
        this.tipoHistorico = tipoHistorico;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }

    public void setRegistradoEm(LocalDateTime registradoEm) {
        this.registradoEm = registradoEm;
    }

}
