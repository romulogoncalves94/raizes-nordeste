package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPedidoEnum;
import com.projeto.raizesnordeste.domain.model.Pedido;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IPedidoPort {
    Pedido save(Pedido pedido);
    Pedido findById(UUID id);
    Page<Pedido> findAll(Integer page, Integer linesPerPage, String direction, String orderBy, CanalPedidoEnum canalPedido);
    Pedido updateStatus(UUID id, StatusPedidoEnum status);
    Pedido cancelar(UUID id);
}
