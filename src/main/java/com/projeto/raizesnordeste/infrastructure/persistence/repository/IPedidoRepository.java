package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.PedidoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IPedidoRepository extends JpaRepository<PedidoEntity, UUID> {
    Page<PedidoEntity> findByCanalPedido(CanalPedidoEnum canalPedido, Pageable pageable);
}
