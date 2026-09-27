package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.ICampanhaPort;
import com.projeto.raizesnordeste.application.ports.IEstoquePort;
import com.projeto.raizesnordeste.application.ports.IPedidoPort;
import com.projeto.raizesnordeste.application.ports.IPedidoRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadePort;
import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.TipoMovimentacaoEstoqueEnum;
import com.projeto.raizesnordeste.domain.model.Campanha;
import com.projeto.raizesnordeste.domain.model.ItemPedido;
import com.projeto.raizesnordeste.domain.model.MovimentacaoEstoque;
import com.projeto.raizesnordeste.domain.model.Pedido;
import com.projeto.raizesnordeste.domain.model.SolicitacaoResgatePontos;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class PedidoService implements IPedidoPort {

    private static final Set<StatusPedidoEnum> STATUS_FINAIS = Set.of(StatusPedidoEnum.CANCELADO, StatusPedidoEnum.ENTREGUE);

    private final IPedidoRepositoryPort repositoryPort;
    private final IUsuarioPort usuarioPort;
    private final IUnidadePort unidadePort;
    private final IProdutoPort produtoPort;
    private final IEstoquePort estoquePort;
    private final IProgramaFidelidadePort programaFidelidadePort;
    private final ICampanhaPort campanhaPort;

    public PedidoService(IPedidoRepositoryPort repositoryPort, IUsuarioPort usuarioPort, IUnidadePort unidadePort,
                         IProdutoPort produtoPort, IEstoquePort estoquePort, IProgramaFidelidadePort programaFidelidadePort,
                         ICampanhaPort campanhaPort) {
        this.repositoryPort = repositoryPort;
        this.usuarioPort = usuarioPort;
        this.unidadePort = unidadePort;
        this.produtoPort = produtoPort;
        this.estoquePort = estoquePort;
        this.programaFidelidadePort = programaFidelidadePort;
        this.campanhaPort = campanhaPort;
    }

    @Override
    @Transactional
    public Pedido save(Pedido pedido) {
        log.info("Iniciando criação de pedido para usuário {} na unidade {} (canal={}, itens={})",
                pedido.getIdUsuario(), pedido.getIdUnidade(), pedido.getCanalPedido(),
                isNull(pedido.getItens()) ? 0 : pedido.getItens().size());

        usuarioPort.findById(pedido.getIdUsuario());
        unidadePort.findById(pedido.getIdUnidade());

        if (isNull(pedido.getItens()) || pedido.getItens().isEmpty()) {
            log.warn("Tentativa de criar pedido sem itens para usuário {}", pedido.getIdUsuario());
            throw new BusinessRuleException("O pedido precisa ter ao menos um item");
        }

        BigDecimal valorBruto = mapValoresBrutoPedido(pedido);
        log.debug("Valor bruto calculado para pedido do usuário {}: R$ {}", pedido.getIdUsuario(), valorBruto);

        BigDecimal valorRestante = valorBruto;
        int pontosResgatados = Optional.ofNullable(pedido.getPontosResgatados()).orElse(0);
        BigDecimal descontoPontos = BigDecimal.ZERO;

        if (pontosResgatados > 0) {
            descontoPontos = BigDecimal.valueOf(pontosResgatados)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);

            if (descontoPontos.compareTo(valorRestante) > 0) {
                log.warn("Desconto de pontos (R$ {}) maior que valor restante do pedido (R$ {}) para usuário {}",
                        descontoPontos, valorRestante, pedido.getIdUsuario());
                throw new BusinessRuleException(
                        String.format("Desconto de pontos (R$%.2f) não pode ser maior que o valor do pedido (R$%.2f)", descontoPontos, valorRestante)
                );
            }

            programaFidelidadePort.resgatar(new SolicitacaoResgatePontos(pedido.getIdUsuario(), pontosResgatados));
            valorRestante = valorRestante.subtract(descontoPontos);
            log.info("Resgatados {} pontos (R$ {} de desconto) para usuário {}", pontosResgatados, descontoPontos, pedido.getIdUsuario());
        }

        Optional<Campanha> campanhaVigente = campanhaPort.findMelhorVigente();
        BigDecimal descontoCampanha = BigDecimal.ZERO;
        UUID idCampanhaAplicada = null;

        if (campanhaVigente.isPresent()) {
            Campanha campanha = campanhaVigente.get();

            descontoCampanha = valorRestante.multiply(campanha.getPercentualDesconto())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);

            valorRestante = valorRestante.subtract(descontoCampanha);
            idCampanhaAplicada = campanha.getId();
            log.info("Campanha '{}' ({}% ) aplicada ao pedido do usuário {}: desconto de R$ {}",
                    campanha.getNome(), campanha.getPercentualDesconto(), pedido.getIdUsuario(), descontoCampanha);
        }

        setValoresTotaisPedido(pedido, valorBruto, descontoPontos, descontoCampanha, valorRestante, pontosResgatados, idCampanhaAplicada);

        Pedido pedidoSalvo = repositoryPort.save(pedido);
        log.info("Pedido {} criado com sucesso para usuário {}: valor total R$ {} (status={})",
                pedidoSalvo.getId(), pedidoSalvo.getIdUsuario(), pedidoSalvo.getValorTotal(), pedidoSalvo.getStatus());

        return pedidoSalvo;
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido findById(UUID id) {
        log.debug("Buscando pedido {}", id);

        Pedido pedido = repositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.info("Pedido {} não encontrado", id);
                    return new ResourceNotFoundException("Pedido não encontrado");
                });

        log.debug("Pedido {} encontrado (status={})", pedido.getId(), pedido.getStatus());
        return pedido;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pedido> findAll(Integer page, Integer linesPerPage, String direction, String orderBy, CanalPedidoEnum canalPedido) {
        log.debug("Listando pedidos: page={} linesPerPage={} direction={} orderBy={} canalPedido={}",
                page, linesPerPage, direction, orderBy, canalPedido);

        PageRequest pageRequest = PageRequest.of(
                page,
                linesPerPage,
                Sort.Direction.valueOf(direction),
                orderBy
        );

        if (nonNull(canalPedido)) {
            return repositoryPort.findAllByCanalPedido(canalPedido, pageRequest);
        }

        return repositoryPort.findAll(pageRequest);
    }

    @Override
    @Transactional
    public Pedido updateStatus(UUID id, StatusPedidoEnum status) {
        log.info("Solicitada alteração de status do pedido {} para {}", id, status);

        Pedido pedido = findById(id);

        validarPedidoNaoFinalizado(pedido);

        StatusPedidoEnum statusAnterior = pedido.getStatus();
        pedido.setStatus(status);

        Pedido pedidoAtualizado = repositoryPort.update(pedido);
        log.info("Pedido {} alterou status de {} para {}", id, statusAnterior, status);

        if (StatusPedidoEnum.ENTREGUE.equals(status)) {
            programaFidelidadePort.acumularPorCompra(pedido.getIdUsuario(), pedido.getValorTotal());
            log.info("Pontos de fidelidade acumulados para usuário {} referentes ao pedido {} (valor R$ {})",
                    pedido.getIdUsuario(), id, pedido.getValorTotal());
        }

        return pedidoAtualizado;
    }

    @Override
    @Transactional
    public Pedido cancelar(UUID id) {
        log.info("Solicitado cancelamento do pedido {}", id);

        Pedido pedido = findById(id);

        validarPedidoNaoFinalizado(pedido);

        for (ItemPedido item : pedido.getItens()) {
            movimentaEstoqueProdutos(pedido, item, TipoMovimentacaoEstoqueEnum.ENTRADA);
        }

        if (nonNull(pedido.getPontosResgatados()) && pedido.getPontosResgatados() > 0) {
            programaFidelidadePort.estornarResgate(pedido.getIdUsuario(), pedido.getPontosResgatados());
            log.info("Estornados {} pontos de fidelidade do usuário {} referentes ao cancelamento do pedido {}",
                    pedido.getPontosResgatados(), pedido.getIdUsuario(), id);
        }

        pedido.setStatus(StatusPedidoEnum.CANCELADO);

        Pedido pedidoCancelado = repositoryPort.update(pedido);
        log.info("Pedido {} cancelado e estoque estornado ({} itens)", id, pedido.getItens().size());

        return pedidoCancelado;
    }

    private void validarPedidoNaoFinalizado(Pedido pedido) {
        if (STATUS_FINAIS.contains(pedido.getStatus())) {
            log.warn("Tentativa de alterar pedido {} já finalizado (status atual: {})", pedido.getId(), pedido.getStatus());
            throw new BusinessRuleException(String.format("Não é possível alterar um pedido com status %s", pedido.getStatus()));
        }
    }

    private static void setValoresTotaisPedido(Pedido pedido, BigDecimal valorBruto, BigDecimal descontoPontos, BigDecimal descontoCampanha, BigDecimal valorRestante, int pontosResgatados, UUID idCampanhaAplicada) {
        pedido.setValorBruto(valorBruto);
        pedido.setValorDescontoPontos(descontoPontos);
        pedido.setValorDescontoCampanha(descontoCampanha);
        pedido.setValorTotal(valorRestante);
        pedido.setPontosResgatados(pontosResgatados);
        pedido.setIdCampanhaAplicada(idCampanhaAplicada);
        pedido.setStatus(StatusPedidoEnum.AGUARDANDO_PAGAMENTO);
    }

    private BigDecimal mapValoresBrutoPedido(Pedido pedido) {
        return pedido.getItens().stream()
                .map(itemPedido -> {
                    var produto = produtoPort.findById(itemPedido.getIdProduto());
                    itemPedido.setNomeProduto(produto.getNome());
                    itemPedido.setPrecoUnitario(produto.getPreco());

                    movimentaEstoqueProdutos(pedido, itemPedido, TipoMovimentacaoEstoqueEnum.SAIDA);

                    return produto.getPreco().multiply(BigDecimal.valueOf(itemPedido.getQuantidade()));
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void movimentaEstoqueProdutos(Pedido pedido, ItemPedido itemPedido, TipoMovimentacaoEstoqueEnum saida) {
        estoquePort.movimentar(new MovimentacaoEstoque(
                pedido.getIdUnidade(),
                itemPedido.getIdProduto(),
                itemPedido.getQuantidade(),
                saida
        ));
    }
}
