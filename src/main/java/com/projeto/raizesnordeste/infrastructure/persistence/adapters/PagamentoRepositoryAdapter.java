package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.IPagamentoRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Pagamento;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.PagamentoEntity;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IPagamentoRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IPedidoRepository;
import com.projeto.raizesnordeste.presentation.mapper.PagamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoRepositoryAdapter implements IPagamentoRepositoryPort {

    private final IPagamentoRepository repository;
    private final IPedidoRepository pedidoRepository;
    private final PagamentoMapper mapper;

    @Override
    public Pagamento save(Pagamento pagamento) {
        PagamentoEntity entity = new PagamentoEntity();
        entity.setPedido(pedidoRepository.getReferenceById(pagamento.getIdPedido()));
        entity.setFormaPagamento(pagamento.getFormaPagamento());
        entity.setStatusPagamento(pagamento.getStatusPagamento());
        entity.setTransacaoGatewayId(pagamento.getTransacaoGatewayId());

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Pagamento> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Pagamento> findByPedidoId(UUID idPedido) {
        return repository.findByPedido_Id(idPedido).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPedidoId(UUID idPedido) {
        return repository.existsByPedido_Id(idPedido);
    }
}
