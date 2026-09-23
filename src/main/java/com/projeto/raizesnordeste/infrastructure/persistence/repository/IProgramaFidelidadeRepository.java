package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.infrastructure.persistence.entities.ProgramaFidelidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IProgramaFidelidadeRepository extends JpaRepository<ProgramaFidelidadeEntity, UUID> {
    Optional<ProgramaFidelidadeEntity> findByUsuario_Id(UUID idUsuario);
}
