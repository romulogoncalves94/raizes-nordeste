package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.infrastructure.persistence.entities.EstoqueEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IEstoqueRepository extends JpaRepository<EstoqueEntity, UUID> {

    Optional<EstoqueEntity> findByUnidade_IdAndProduto_Id(UUID idUnidade, UUID idProduto);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EstoqueEntity e WHERE e.unidade.id = :idUnidade AND e.produto.id = :idProduto")
    Optional<EstoqueEntity> lockByUnidadeAndProduto(@Param("idUnidade") UUID idUnidade, @Param("idProduto") UUID idProduto);
}
