package com.projeto.raizesnordeste.domain.model;

import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPedidoEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pedido {
    private UUID id;
    private UUID idUsuario;
    private String nomeUsuario;
    private UUID idUnidade;
    private String nomeUnidade;
    private CanalPedidoEnum canalPedido;
    private StatusPedidoEnum status;
    private BigDecimal valorTotal;
    private List<ItemPedido> itens = new ArrayList<>();
    private Integer pontosResgatados;

    public Pedido() {
    }

    public Pedido(UUID id, UUID idUsuario, String nomeUsuario, UUID idUnidade, String nomeUnidade,
                  CanalPedidoEnum canalPedido, StatusPedidoEnum status, BigDecimal valorTotal, List<ItemPedido> itens) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.idUnidade = idUnidade;
        this.nomeUnidade = nomeUnidade;
        this.canalPedido = canalPedido;
        this.status = status;
        this.valorTotal = valorTotal;
        this.itens = itens;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public UUID getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(UUID idUnidade) {
        this.idUnidade = idUnidade;
    }

    public String getNomeUnidade() {
        return nomeUnidade;
    }

    public void setNomeUnidade(String nomeUnidade) {
        this.nomeUnidade = nomeUnidade;
    }

    public CanalPedidoEnum getCanalPedido() {
        return canalPedido;
    }

    public void setCanalPedido(CanalPedidoEnum canalPedido) {
        this.canalPedido = canalPedido;
    }

    public StatusPedidoEnum getStatus() {
        return status;
    }

    public void setStatus(StatusPedidoEnum status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public Integer getPontosResgatados() {
        return pontosResgatados;
    }

    public void setPontosResgatados(Integer pontosResgatados) {
        this.pontosResgatados = pontosResgatados;
    }

}
