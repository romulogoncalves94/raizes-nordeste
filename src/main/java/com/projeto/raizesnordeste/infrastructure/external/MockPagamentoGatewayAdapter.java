package com.projeto.raizesnordeste.infrastructure.external;

import com.projeto.raizesnordeste.application.ports.IPagamentoGatewayPort;
import com.projeto.raizesnordeste.domain.enums.FormaPagamentoEnum;
import com.projeto.raizesnordeste.domain.model.ResultadoPagamentoGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

// Simula o gateway externo de pagamento do diagrama do PROMPT.md ("Serviço Externo: Mock Pagamento")
@Component
public class MockPagamentoGatewayAdapter implements IPagamentoGatewayPort {

    @Override
    public ResultadoPagamentoGateway processar(FormaPagamentoEnum formaPagamento, BigDecimal valor, boolean simularFalha) {
        boolean aprovado = !simularFalha;
        String transacaoId = "MOCK-" + UUID.randomUUID();

        return new ResultadoPagamentoGateway(aprovado, transacaoId);
    }
}
