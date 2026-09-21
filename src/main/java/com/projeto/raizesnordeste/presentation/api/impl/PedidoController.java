package com.projeto.raizesnordeste.presentation.api.impl;

import com.projeto.raizesnordeste.application.ports.IPedidoPort;
import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.model.Pedido;
import com.projeto.raizesnordeste.presentation.api.IPedidoController;
import com.projeto.raizesnordeste.presentation.mapper.PedidoMapper;
import com.projeto.raizesnordeste.presentation.records.pedido.CreatedPedidoRequest;
import com.projeto.raizesnordeste.presentation.records.pedido.PedidoResponse;
import com.projeto.raizesnordeste.presentation.records.pedido.UpdateStatusPedidoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController implements IPedidoController {

    private final IPedidoPort useCase;
    private final PedidoMapper mapper;

    @Override
    public ResponseEntity<PedidoResponse> save(CreatedPedidoRequest request) {
        Pedido pedido = mapper.toDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PedidoResponse.from(useCase.save(pedido)));
    }

    @Override
    public ResponseEntity<PedidoResponse> findById(UUID id) {
        return ResponseEntity.ok(PedidoResponse.from(useCase.findById(id)));
    }

    @Override
    public ResponseEntity<Page<PedidoResponse>> findAll(Integer page, Integer linesPerPage, String direction, String orderBy, CanalPedidoEnum canalPedido) {
        return ResponseEntity.ok(useCase.findAll(page, linesPerPage, direction, orderBy, canalPedido)
                .map(PedidoResponse::from));
    }

    @Override
    public ResponseEntity<PedidoResponse> updateStatus(UUID id, UpdateStatusPedidoRequest request) {
        return ResponseEntity.ok(PedidoResponse.from(useCase.updateStatus(id, request.status())));
    }

    @Override
    public ResponseEntity<PedidoResponse> cancelar(UUID id) {
        return ResponseEntity.ok(PedidoResponse.from(useCase.cancelar(id)));
    }
}
