package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IPedidoRepositoryPort {
    Pedido save(Pedido pedido);
    Optional<Pedido> findById(UUID id);
    Page<Pedido> findAll(Pageable pageable);
    Page<Pedido> findAllByCanalPedido(CanalPedidoEnum canalPedido, Pageable pageable);
    Pedido update(Pedido pedido);
}
