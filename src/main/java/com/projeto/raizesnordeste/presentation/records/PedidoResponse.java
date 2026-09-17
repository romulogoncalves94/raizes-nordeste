package com.projeto.raizesnordeste.presentation.records;

import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPedidoEnum;
import com.projeto.raizesnordeste.domain.model.Pedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        UUID id,
        UUID idUsuario,
        String nomeUsuario,
        UUID idUnidade,
        String nomeUnidade,
        CanalPedidoEnum canalPedido,
        StatusPedidoEnum status,
        BigDecimal valorTotal,
        Integer pontosResgatados,
        List<ItemPedidoResponse> itens
) {
    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getIdUsuario(),
                pedido.getNomeUsuario(),
                pedido.getIdUnidade(),
                pedido.getNomeUnidade(),
                pedido.getCanalPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getPontosResgatados(),
                pedido.getItens().stream().map(ItemPedidoResponse::from).toList()
        );
    }
}
