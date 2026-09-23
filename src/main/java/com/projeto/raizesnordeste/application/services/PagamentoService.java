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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        Pedido pedido = pedidoPort.findById(solicitacao.getIdPedido());

        if (!StatusPedidoEnum.AGUARDANDO_PAGAMENTO.equals(pedido.getStatus())) {
            throw new BusinessRuleException(String.format("Pedido não está aguardando pagamento (status atual: %s)", pedido.getStatus()));
        }

        if (repositoryPort.existsByPedidoId(pedido.getId())) {
            throw new BusinessRuleException("Já existe um pagamento registrado para este pedido");
        }

        boolean simularFalha = Boolean.TRUE.equals(solicitacao.getSimularFalha());
        ResultadoPagamentoGateway resultadoPagamento = gatewayPort.processar(solicitacao.getFormaPagamento(), pedido.getValorTotal(), simularFalha);

        Pagamento pagamento = buildPagamento(pedido.getId(), solicitacao.getFormaPagamento(), resultadoPagamento);
        Pagamento pagamentoSalvo = repositoryPort.save(pagamento);

        if (StatusPagamentoEnum.APROVADO.equals(pagamentoSalvo.getStatusPagamento())) {
            pedidoPort.updateStatus(pedido.getId(), StatusPedidoEnum.COZINHA);
            return pagamentoSalvo;
        }

        pedidoPort.cancelar(pedido.getId());

        throw new PaymentRequiredException(
                String.format("Pagamento recusado pelo gateway (transacaoId: %s). Pedido cancelado e estoque estornado."
                        , pagamentoSalvo.getTransacaoGatewayId())
        );
    }

    @Override
    public Pagamento findById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado"));
    }

    @Override
    public Pagamento findByPedido(UUID idPedido) {
        return repositoryPort.findByPedidoId(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para o pedido informado"));
    }

    private Pagamento buildPagamento(UUID idPedido, FormaPagamentoEnum formaPagamento, ResultadoPagamentoGateway resultadoPagamento) {
        Pagamento pagamento = new Pagamento();
        pagamento.setIdPedido(idPedido);
        pagamento.setFormaPagamento(formaPagamento);
        pagamento.setTransacaoGatewayId(resultadoPagamento.getTransacaoId());
        pagamento.setStatusPagamento(Boolean.TRUE.equals(resultadoPagamento.getAprovado())
                ? StatusPagamentoEnum.APROVADO
                : StatusPagamentoEnum.RECUSADO);

        return pagamento;
    }
}
