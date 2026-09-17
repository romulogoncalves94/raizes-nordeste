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
        BigDecimal valorBruto,
        Integer pontosResgatados,
        BigDecimal valorDescontoPontos,
        UUID idCampanhaAplicada,
        String nomeCampanhaAplicada,
        BigDecimal valorDescontoCampanha,
        BigDecimal valorTotal,
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
                pedido.getValorBruto(),
                pedido.getPontosResgatados(),
                pedido.getValorDescontoPontos(),
                pedido.getIdCampanhaAplicada(),
                pedido.getNomeCampanhaAplicada(),
                pedido.getValorDescontoCampanha(),
                pedido.getValorTotal(),
                pedido.getItens().stream().map(ItemPedidoResponse::from).toList()
        );
    }
}
