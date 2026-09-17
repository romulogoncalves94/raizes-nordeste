package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.infrastructure.persistence.entities.PagamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPagamentoRepository extends JpaRepository<PagamentoEntity, UUID> {
    Optional<PagamentoEntity> findByPedido_Id(UUID idPedido);
    boolean existsByPedido_Id(UUID idPedido);
}
