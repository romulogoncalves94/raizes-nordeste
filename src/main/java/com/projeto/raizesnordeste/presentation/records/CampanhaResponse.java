package com.projeto.raizesnordeste.presentation.records;

import com.projeto.raizesnordeste.domain.model.Campanha;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CampanhaResponse(
        UUID id,
        String nome,
        BigDecimal percentualDesconto,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Boolean ativa,
        boolean vigente
) {
    public static CampanhaResponse from(Campanha campanha) {
        LocalDateTime agora = LocalDateTime.now();
        boolean vigente = Boolean.TRUE.equals(campanha.getAtiva())
                && !agora.isBefore(campanha.getDataInicio())
                && !agora.isAfter(campanha.getDataFim());

        return new CampanhaResponse(
                campanha.getId(),
                campanha.getNome(),
                campanha.getPercentualDesconto(),
                campanha.getDataInicio(),
                campanha.getDataFim(),
                campanha.getAtiva(),
                vigente
        );
    }
}
