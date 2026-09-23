package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.IPedidoRepositoryPort;
import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
        PedidoEntity entity = buildPedidoEntity(pedido);

        List<ItemPedidoEntity> itensEntity = pedido.getItens().stream()
                .map(itemPedido -> ItemPedidoEntity.builder()
                        .pedido(entity)
                        .produto(produtoRepository.getReferenceById(itemPedido.getIdProduto()))
                        .quantidade(itemPedido.getQuantidade())
                        .precoUnitario(itemPedido.getPrecoUnitario())
                        .build())
                .toList();

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

    private PedidoEntity buildPedidoEntity(Pedido pedido) {
        return PedidoEntity.builder()
                .usuario(usuarioRepository.getReferenceById(pedido.getIdUsuario()))
                .unidade(unidadeRepository.getReferenceById(pedido.getIdUnidade()))
                .canalPedido(pedido.getCanalPedido())
                .status(pedido.getStatus())
                .valorBruto(valorOuZero(pedido.getValorBruto()))
                .valorDescontoPontos(valorOuZero(pedido.getValorDescontoPontos()))
                .valorDescontoCampanha(valorOuZero(pedido.getValorDescontoCampanha()))
                .valorTotal(pedido.getValorTotal())
                .pontosResgatados(nonNull(pedido.getPontosResgatados()) ? pedido.getPontosResgatados() : 0)
                .campanhaAplicada(nonNull(pedido.getIdCampanhaAplicada()) ? campanhaRepository.getReferenceById(pedido.getIdCampanhaAplicada()) : null)
                .build();
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return isNull(valor) ? BigDecimal.ZERO : valor;
    }
}
