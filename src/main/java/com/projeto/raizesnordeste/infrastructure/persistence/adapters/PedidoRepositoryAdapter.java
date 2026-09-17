package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.IPedidoRepositoryPort;
import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.model.ItemPedido;
import com.projeto.raizesnordeste.domain.model.Pedido;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.ItemPedidoEntity;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.PedidoEntity;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.ICampanhaRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IPedidoRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IProdutoRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IUnidadeRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IUsuarioRepository;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import com.projeto.raizesnordeste.presentation.mapper.PedidoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoRepositoryAdapter implements IPedidoRepositoryPort {

    private final IPedidoRepository repository;
    private final IUsuarioRepository usuarioRepository;
    private final IUnidadeRepository unidadeRepository;
    private final IProdutoRepository produtoRepository;
    private final ICampanhaRepository campanhaRepository;
    private final PedidoMapper mapper;

    @Override
    public Pedido save(Pedido pedido) {
        PedidoEntity entity = new PedidoEntity();
        preencherEntity(entity, pedido);

        List<ItemPedidoEntity> itensEntity = new ArrayList<>();
        for (ItemPedido item : pedido.getItens()) {
            ItemPedidoEntity itemEntity = new ItemPedidoEntity();
            itemEntity.setPedido(entity);
            itemEntity.setProduto(produtoRepository.getReferenceById(item.getIdProduto()));
            itemEntity.setQuantidade(item.getQuantidade());
            itemEntity.setPrecoUnitario(item.getPrecoUnitario());
            itensEntity.add(itemEntity);
        }
        entity.setItens(itensEntity);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Pedido> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Pedido> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Pedido> findAllByCanalPedido(CanalPedidoEnum canalPedido, Pageable pageable) {
        return repository.findByCanalPedido(canalPedido, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Pedido update(Pedido pedido) {
        PedidoEntity entity = repository.findById(pedido.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        entity.setStatus(pedido.getStatus());

        return mapper.toDomain(repository.save(entity));
    }

    private void preencherEntity(PedidoEntity entity, Pedido pedido) {
        entity.setUsuario(usuarioRepository.getReferenceById(pedido.getIdUsuario()));
        entity.setUnidade(unidadeRepository.getReferenceById(pedido.getIdUnidade()));
        entity.setCanalPedido(pedido.getCanalPedido());
        entity.setStatus(pedido.getStatus());
        entity.setValorBruto(valorOuZero(pedido.getValorBruto()));
        entity.setValorDescontoPontos(valorOuZero(pedido.getValorDescontoPontos()));
        entity.setValorDescontoCampanha(valorOuZero(pedido.getValorDescontoCampanha()));
        entity.setValorTotal(pedido.getValorTotal());
        entity.setPontosResgatados(pedido.getPontosResgatados() != null ? pedido.getPontosResgatados() : 0);
        entity.setCampanhaAplicada(pedido.getIdCampanhaAplicada() != null
                ? campanhaRepository.getReferenceById(pedido.getIdCampanhaAplicada())
                : null);
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
