package com.projeto.raizesnordeste.presentation.records;

import com.projeto.raizesnordeste.domain.model.ProgramaFidelidade;

import java.util.UUID;

public record ProgramaFidelidadeResponse(
        UUID id,
        UUID idUsuario,
        String nomeUsuario,
        Integer saldoPontos
) {
    public static ProgramaFidelidadeResponse from(ProgramaFidelidade programa) {
        return new ProgramaFidelidadeResponse(
                programa.getId(),
                programa.getIdUsuario(),
                programa.getNomeUsuario(),
                programa.getSaldoPontos()
        );
    }
}
