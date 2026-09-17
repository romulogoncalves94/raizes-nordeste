package com.projeto.raizesnordeste.domain.model;

import com.projeto.raizesnordeste.domain.enums.FormaPagamentoEnum;

import java.util.UUID;

public class SolicitacaoPagamento {
    private UUID idPedido;
    private FormaPagamentoEnum formaPagamento;
    private Boolean simularFalha;

    public SolicitacaoPagamento() {
    }

    public SolicitacaoPagamento(UUID idPedido, FormaPagamentoEnum formaPagamento, Boolean simularFalha) {
        this.idPedido = idPedido;
        this.formaPagamento = formaPagamento;
        this.simularFalha = simularFalha;
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

    public Boolean getSimularFalha() {
        return simularFalha;
    }

    public void setSimularFalha(Boolean simularFalha) {
        this.simularFalha = simularFalha;
    }

}
