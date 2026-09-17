package com.projeto.raizesnordeste.domain.model;

public class ResultadoPagamentoGateway {
    private Boolean aprovado;
    private String transacaoId;

    public ResultadoPagamentoGateway() {
    }

    public ResultadoPagamentoGateway(Boolean aprovado, String transacaoId) {
        this.aprovado = aprovado;
        this.transacaoId = transacaoId;
    }

    public Boolean getAprovado() {
        return aprovado;
    }

    public void setAprovado(Boolean aprovado) {
        this.aprovado = aprovado;
    }

    public String getTransacaoId() {
        return transacaoId;
    }

    public void setTransacaoId(String transacaoId) {
        this.transacaoId = transacaoId;
    }

}
