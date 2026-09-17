package com.projeto.raizesnordeste.presentation.records;

import com.projeto.raizesnordeste.domain.enums.TipoHistoricoPontosEnum;
import com.projeto.raizesnordeste.domain.model.HistoricoPontos;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoPontosResponse(
        UUID id,
        Integer pontos,
        TipoHistoricoPontosEnum tipoHistorico,
        LocalDateTime registradoEm
) {
    public static HistoricoPontosResponse from(HistoricoPontos historico) {
        return new HistoricoPontosResponse(
                historico.getId(),
                historico.getPontos(),
                historico.getTipoHistorico(),
                historico.getRegistradoEm()
        );
    }
}
