package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IEstoquePort;
import com.projeto.raizesnordeste.application.ports.IPedidoPort;
import com.projeto.raizesnordeste.application.ports.IPedidoRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.TipoMovimentacaoEstoqueEnum;
import com.projeto.raizesnordeste.domain.model.ItemPedido;
import com.projeto.raizesnordeste.domain.model.MovimentacaoEstoque;
import com.projeto.raizesnordeste.domain.model.Pedido;
import com.projeto.raizesnordeste.domain.model.Produto;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public class PedidoService implements IPedidoPort {

    private static final Set<StatusPedidoEnum> STATUS_FINAIS = Set.of(StatusPedidoEnum.CANCELADO, StatusPedidoEnum.ENTREGUE);

    private final IPedidoRepositoryPort repositoryPort;
    private final IUsuarioPort usuarioPort;
    private final IUnidadePort unidadePort;
    private final IProdutoPort produtoPort;
    private final IEstoquePort estoquePort;

    public PedidoService(IPedidoRepositoryPort repositoryPort, IUsuarioPort usuarioPort, IUnidadePort unidadePort,
                          IProdutoPort produtoPort, IEstoquePort estoquePort) {
        this.repositoryPort = repositoryPort;
        this.usuarioPort = usuarioPort;
        this.unidadePort = unidadePort;
        this.produtoPort = produtoPort;
        this.estoquePort = estoquePort;
    }

    @Override
    @Transactional
    public Pedido save(Pedido pedido) {
        usuarioPort.findById(pedido.getIdUsuario());
        unidadePort.findById(pedido.getIdUnidade());

        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new BusinessRuleException("O pedido precisa ter ao menos um item");
        }

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = produtoPort.findById(item.getIdProduto());
            item.setNomeProduto(produto.getNome());
            item.setPrecoUnitario(produto.getPreco());
            valorTotal = valorTotal.add(produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        for (ItemPedido item : pedido.getItens()) {
            estoquePort.movimentar(new MovimentacaoEstoque(
                    pedido.getIdUnidade(), item.getIdProduto(), item.getQuantidade(), TipoMovimentacaoEstoqueEnum.SAIDA
            ));
        }

        pedido.setValorTotal(valorTotal);
        pedido.setStatus(StatusPedidoEnum.AGUARDANDO_PAGAMENTO);

        return repositoryPort.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido findById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pedido> findAll(Integer page, Integer linesPerPage, String direction, String orderBy, CanalPedidoEnum canalPedido) {
        PageRequest pageRequest = PageRequest.of(
                page,
                linesPerPage,
                Sort.Direction.valueOf(direction),
                orderBy
        );

        if (canalPedido != null) {
            return repositoryPort.findAllByCanalPedido(canalPedido, pageRequest);
        }

        return repositoryPort.findAll(pageRequest);
    }

    @Override
    @Transactional
    public Pedido updateStatus(UUID id, StatusPedidoEnum status) {
        Pedido pedido = findById(id);

        validarPedidoNaoFinalizado(pedido);

        pedido.setStatus(status);

        return repositoryPort.update(pedido);
    }

    @Override
    @Transactional
    public Pedido cancelar(UUID id) {
        Pedido pedido = findById(id);

        validarPedidoNaoFinalizado(pedido);

        for (ItemPedido item : pedido.getItens()) {
            estoquePort.movimentar(new MovimentacaoEstoque(
                    pedido.getIdUnidade(), item.getIdProduto(), item.getQuantidade(), TipoMovimentacaoEstoqueEnum.ENTRADA
            ));
        }

        pedido.setStatus(StatusPedidoEnum.CANCELADO);

        return repositoryPort.update(pedido);
    }

    private void validarPedidoNaoFinalizado(Pedido pedido) {
        if (STATUS_FINAIS.contains(pedido.getStatus())) {
            throw new BusinessRuleException("Não é possível alterar um pedido com status " + pedido.getStatus());
        }
    }
}
