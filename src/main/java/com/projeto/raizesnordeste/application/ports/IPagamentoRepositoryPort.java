package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Pagamento;

import java.util.Optional;
import java.util.UUID;

public interface IPagamentoRepositoryPort {
    Pagamento save(Pagamento pagamento);
    Optional<Pagamento> findById(UUID id);
    Optional<Pagamento> findByPedidoId(UUID idPedido);
    boolean existsByPedidoId(UUID idPedido);
}
