package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IPagamentoGatewayPort;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagamentoService")
class PagamentoServiceTest {

    @Mock
    private IPagamentoRepositoryPort repositoryPort;

    @Mock
    private IPagamentoGatewayPort gatewayPort;

    @Mock
    private IPedidoPort pedidoPort;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Pedido umPedidoAguardandoPagamento(UUID id) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setStatus(StatusPedidoEnum.AGUARDANDO_PAGAMENTO);
        pedido.setValorTotal(new BigDecimal("50.00"));
        return pedido;
    }

    private SolicitacaoPagamento umaSolicitacao(UUID idPedido, boolean simularFalha) {
        return new SolicitacaoPagamento(idPedido, FormaPagamentoEnum.PIX, simularFalha);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido não é encontrado ao processar pagamento")
    void deveLancarResourceNotFoundException_quandoPedidoNaoEncontrado() {
        UUID idPedido = UUID.randomUUID();
        when(pedidoPort.findById(idPedido)).thenThrow(new ResourceNotFoundException("Pedido não encontrado"));

        assertThatThrownBy(() -> pagamentoService.processar(umaSolicitacao(idPedido, false)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando pedido não está aguardando pagamento")
    void deveLancarBusinessRuleException_quandoPedidoNaoEstaAguardandoPagamento() {
        UUID idPedido = UUID.randomUUID();
        Pedido pedido = umPedidoAguardandoPagamento(idPedido);
        pedido.setStatus(StatusPedidoEnum.COZINHA);
        when(pedidoPort.findById(idPedido)).thenReturn(pedido);

        assertThatThrownBy(() -> pagamentoService.processar(umaSolicitacao(idPedido, false)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("COZINHA");
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando pedido já possui pagamento registrado")
    void deveLancarBusinessRuleException_quandoPedidoJaPossuiPagamento() {
        UUID idPedido = UUID.randomUUID();
        Pedido pedido = umPedidoAguardandoPagamento(idPedido);
        when(pedidoPort.findById(idPedido)).thenReturn(pedido);
        when(repositoryPort.existsByPedidoId(idPedido)).thenReturn(true);

        assertThatThrownBy(() -> pagamentoService.processar(umaSolicitacao(idPedido, false)))
                .isInstanceOf(BusinessRuleException.class);

        verify(gatewayPort, never()).processar(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Deve aprovar pagamento e avançar pedido para cozinha quando gateway aprova")
    void deveAprovarPagamentoEAvancarPedidoParaCozinha_quandoGatewayAprova() {
        UUID idPedido = UUID.randomUUID();
        Pedido pedido = umPedidoAguardandoPagamento(idPedido);
        when(pedidoPort.findById(idPedido)).thenReturn(pedido);
        when(repositoryPort.existsByPedidoId(idPedido)).thenReturn(false);
        when(gatewayPort.processar(FormaPagamentoEnum.PIX, pedido.getValorTotal(), false))
                .thenReturn(new ResultadoPagamentoGateway(true, "TX-APROVADO"));
        when(repositoryPort.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento resultado = pagamentoService.processar(umaSolicitacao(idPedido, false));

        assertThat(resultado.getStatusPagamento()).isEqualTo(StatusPagamentoEnum.APROVADO);
        verify(pedidoPort).updateStatus(idPedido, StatusPedidoEnum.COZINHA);
        verify(pedidoPort, never()).cancelar(any());
    }

    @Test
    @DisplayName("Deve cancelar pedido e lançar PaymentRequiredException quando gateway recusa")
    void deveCancelarPedidoELancarPaymentRequiredException_quandoGatewayRecusa() {
        UUID idPedido = UUID.randomUUID();
        Pedido pedido = umPedidoAguardandoPagamento(idPedido);
        when(pedidoPort.findById(idPedido)).thenReturn(pedido);
        when(repositoryPort.existsByPedidoId(idPedido)).thenReturn(false);
        when(gatewayPort.processar(FormaPagamentoEnum.PIX, pedido.getValorTotal(), true))
                .thenReturn(new ResultadoPagamentoGateway(false, "TX-RECUSADO"));
        when(repositoryPort.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> pagamentoService.processar(umaSolicitacao(idPedido, true)))
                .isInstanceOf(PaymentRequiredException.class)
                .hasMessageContaining("TX-RECUSADO");

        verify(pedidoPort).cancelar(idPedido);
        verify(pedidoPort, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pagamento não é encontrado por id")
    void deveLancarResourceNotFoundException_quandoPagamentoNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pagamento não é encontrado por pedido")
    void deveLancarResourceNotFoundException_quandoPagamentoNaoEncontradoPorPedido() {
        UUID idPedido = UUID.randomUUID();
        when(repositoryPort.findByPedidoId(idPedido)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.findByPedido(idPedido))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
