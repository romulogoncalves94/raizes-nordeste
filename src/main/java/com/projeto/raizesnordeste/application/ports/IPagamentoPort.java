package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Pagamento;
import com.projeto.raizesnordeste.domain.model.SolicitacaoPagamento;

import java.util.UUID;

public interface IPagamentoPort {
    Pagamento processar(SolicitacaoPagamento solicitacao);
    Pagamento findById(UUID id);
    Pagamento findByPedido(UUID idPedido);
}
