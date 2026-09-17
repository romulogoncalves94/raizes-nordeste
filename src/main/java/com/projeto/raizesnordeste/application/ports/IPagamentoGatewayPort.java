package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.enums.FormaPagamentoEnum;
import com.projeto.raizesnordeste.domain.model.ResultadoPagamentoGateway;

import java.math.BigDecimal;

public interface IPagamentoGatewayPort {
    ResultadoPagamentoGateway processar(FormaPagamentoEnum formaPagamento, BigDecimal valor, boolean simularFalha);
}
