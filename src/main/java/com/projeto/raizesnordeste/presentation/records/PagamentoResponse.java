package com.projeto.raizesnordeste.presentation.records;

import com.projeto.raizesnordeste.domain.enums.FormaPagamentoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPagamentoEnum;
import com.projeto.raizesnordeste.domain.model.Pagamento;

import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,
        UUID idPedido,
        FormaPagamentoEnum formaPagamento,
        StatusPagamentoEnum statusPagamento,
        String transacaoGatewayId,
        LocalDateTime dataProcessamento
) {
    public static PagamentoResponse from(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getIdPedido(),
                pagamento.getFormaPagamento(),
                pagamento.getStatusPagamento(),
                pagamento.getTransacaoGatewayId(),
                pagamento.getDataProcessamento()
        );
    }
}
