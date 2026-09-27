package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IPagamentoGatewayPort;
import com.projeto.raizesnordeste.application.ports.IPagamentoPort;
import com.projeto.raizesnordeste.application.ports.IPagamentoRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IPedidoPort;
import com.projeto.raizesnordeste.domain.enums.FormaPagamentoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPagamentoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPedidoEnum;
import com.projeto.raizesnordeste.domain.model.Pagamento;
import com.projeto.raizesnordeste.domain.model.Pedido;
import com.projeto.raizesnordeste.domain.model.ResultadoPagamentoGateway;
import com.projeto.raizesnordeste.domain.model.SolicitacaoPagamento;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.PaymentRequiredException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
public class PagamentoService implements IPagamentoPort {

    private final IPagamentoRepositoryPort repositoryPort;
    private final IPagamentoGatewayPort gatewayPort;
    private final IPedidoPort pedidoPort;

    public PagamentoService(IPagamentoRepositoryPort repositoryPort, IPagamentoGatewayPort gatewayPort, IPedidoPort pedidoPort) {
        this.repositoryPort = repositoryPort;
        this.gatewayPort = gatewayPort;
        this.pedidoPort = pedidoPort;
    }

    @Override
    @Transactional(noRollbackFor = PaymentRequiredException.class)
    public Pagamento processar(SolicitacaoPagamento solicitacao) {
        log.info("Iniciando processamento de pagamento para pedido {} (forma={})",
                solicitacao.getIdPedido(), solicitacao.getFormaPagamento());

        Pedido pedido = pedidoPort.findById(solicitacao.getIdPedido());

        if (!StatusPedidoEnum.AGUARDANDO_PAGAMENTO.equals(pedido.getStatus())) {
            log.warn("Pedido {} não está aguardando pagamento (status atual: {})", pedido.getId(), pedido.getStatus());
            throw new BusinessRuleException(String.format("Pedido não está aguardando pagamento (status atual: %s)", pedido.getStatus()));
        }

        if (repositoryPort.existsByPedidoId(pedido.getId())) {
            log.warn("Tentativa de pagamento duplicado para pedido {}", pedido.getId());
            throw new BusinessRuleException("Já existe um pagamento registrado para este pedido");
        }

        boolean simularFalha = Boolean.TRUE.equals(solicitacao.getSimularFalha());
        ResultadoPagamentoGateway resultadoPagamento = gatewayPort.processar(solicitacao.getFormaPagamento(), pedido.getValorTotal(), simularFalha);
        log.debug("Gateway retornou transacaoId={} aprovado={} para pedido {}",
                resultadoPagamento.getTransacaoId(), resultadoPagamento.getAprovado(), pedido.getId());

        Pagamento pagamento = buildPagamento(pedido.getId(), solicitacao.getFormaPagamento(), resultadoPagamento);
        Pagamento pagamentoSalvo = repositoryPort.save(pagamento);

        if (StatusPagamentoEnum.APROVADO.equals(pagamentoSalvo.getStatusPagamento())) {
            pedidoPort.updateStatus(pedido.getId(), StatusPedidoEnum.COZINHA);
            log.info("Pagamento {} aprovado para pedido {} (transacaoId={}, valor R$ {})",
                    pagamentoSalvo.getId(), pedido.getId(), pagamentoSalvo.getTransacaoGatewayId(), pedido.getValorTotal());
            return pagamentoSalvo;
        }

        pedidoPort.cancelar(pedido.getId());
        log.warn("Pagamento {} recusado para pedido {} (transacaoId={}). Pedido cancelado e estoque estornado.",
                pagamentoSalvo.getId(), pedido.getId(), pagamentoSalvo.getTransacaoGatewayId());

        throw new PaymentRequiredException(
                String.format("Pagamento recusado pelo gateway (transacaoId: %s). Pedido cancelado e estoque estornado."
                        , pagamentoSalvo.getTransacaoGatewayId())
        );
    }

    @Override
    public Pagamento findById(UUID id) {
        log.debug("Buscando pagamento {}", id);

        Pagamento pagamento = repositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.info("Pagamento {} não encontrado", id);
                    return new ResourceNotFoundException("Pagamento não encontrado");
                });

        log.debug("Pagamento {} encontrado (status={})", pagamento.getId(), pagamento.getStatusPagamento());
        return pagamento;
    }

    @Override
    public Pagamento findByPedido(UUID idPedido) {
        log.debug("Buscando pagamento do pedido {}", idPedido);

        Pagamento pagamento = repositoryPort.findByPedidoId(idPedido)
                .orElseThrow(() -> {
                    log.info("Pagamento não encontrado para pedido {}", idPedido);
                    return new ResourceNotFoundException("Pagamento não encontrado para o pedido informado");
                });

        log.debug("Pagamento {} encontrado para pedido {} (status={})", pagamento.getId(), idPedido, pagamento.getStatusPagamento());
        return pagamento;
    }

    private Pagamento buildPagamento(UUID idPedido, FormaPagamentoEnum formaPagamento, ResultadoPagamentoGateway resultadoPagamento) {
        Pagamento pagamento = new Pagamento();
        pagamento.setIdPedido(idPedido);
        pagamento.setFormaPagamento(formaPagamento);
        pagamento.setTransacaoGatewayId(resultadoPagamento.getTransacaoId());
        pagamento.setStatusPagamento(Boolean.TRUE.equals(resultadoPagamento.getAprovado())
                ? StatusPagamentoEnum.APROVADO
                : StatusPagamentoEnum.RECUSADO);
        pagamento.setDataProcessamento(LocalDateTime.now());

        return pagamento;
    }
}
