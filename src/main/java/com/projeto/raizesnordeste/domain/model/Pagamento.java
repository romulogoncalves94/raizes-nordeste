package com.projeto.raizesnordeste.domain.model;

import com.projeto.raizesnordeste.domain.enums.FormaPagamentoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPagamentoEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public class Pagamento {
    private UUID id;
    private UUID idPedido;
    private FormaPagamentoEnum formaPagamento;
    private StatusPagamentoEnum statusPagamento;
    private String transacaoGatewayId;
    private LocalDateTime dataProcessamento;

    public Pagamento() {
    }

    public Pagamento(UUID id, UUID idPedido, FormaPagamentoEnum formaPagamento, StatusPagamentoEnum statusPagamento,
                      String transacaoGatewayId, LocalDateTime dataProcessamento) {
        this.id = id;
        this.idPedido = idPedido;
        this.formaPagamento = formaPagamento;
        this.statusPagamento = statusPagamento;
        this.transacaoGatewayId = transacaoGatewayId;
        this.dataProcessamento = dataProcessamento;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(UUID idPedido) {
        this.idPedido = idPedido;
    }

    public FormaPagamentoEnum getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamentoEnum formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public StatusPagamentoEnum getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamentoEnum statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public String getTransacaoGatewayId() {
        return transacaoGatewayId;
    }

    public void setTransacaoGatewayId(String transacaoGatewayId) {
        this.transacaoGatewayId = transacaoGatewayId;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }

    public void setDataProcessamento(LocalDateTime dataProcessamento) {
        this.dataProcessamento = dataProcessamento;
    }

}
