package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.application.ports.IPagamentoPort;
import com.projeto.raizesnordeste.domain.model.SolicitacaoPagamento;
import com.projeto.raizesnordeste.presentation.mapper.PagamentoMapper;
import com.projeto.raizesnordeste.presentation.records.CreatedPagamentoRequest;
import com.projeto.raizesnordeste.presentation.records.PagamentoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentoController implements IPagamentoController {

    private final IPagamentoPort useCase;
    private final PagamentoMapper mapper;

    @Override
    public ResponseEntity<PagamentoResponse> save(CreatedPagamentoRequest request) {
        SolicitacaoPagamento solicitacao = mapper.toDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PagamentoResponse.from(useCase.processar(solicitacao)));
    }

    @Override
    public ResponseEntity<PagamentoResponse> findById(UUID id) {
        return ResponseEntity.ok(PagamentoResponse.from(useCase.findById(id)));
    }

    @Override
    public ResponseEntity<PagamentoResponse> findByPedido(UUID idPedido) {
        return ResponseEntity.ok(PagamentoResponse.from(useCase.findByPedido(idPedido)));
    }
}
