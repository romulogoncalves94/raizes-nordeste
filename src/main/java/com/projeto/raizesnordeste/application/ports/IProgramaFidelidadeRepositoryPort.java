package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.ProgramaFidelidade;

import java.util.Optional;
import java.util.UUID;

public interface IProgramaFidelidadeRepositoryPort {
    ProgramaFidelidade save(ProgramaFidelidade programa);
    Optional<ProgramaFidelidade> findByUsuarioId(UUID idUsuario);
    boolean existsByUsuarioId(UUID idUsuario);
    ProgramaFidelidade update(ProgramaFidelidade programa);
}
