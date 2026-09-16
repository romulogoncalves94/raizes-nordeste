package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.infrastructure.persistence.entities.UnidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUnidadeRepository extends JpaRepository<UnidadeEntity, UUID> {
    Optional<UnidadeEntity> findByCnpj(String cnpj);
}
