package com.projeto.raizesnordeste.presentation.api.impl;

import com.projeto.raizesnordeste.application.ports.IEstoquePort;
import com.projeto.raizesnordeste.domain.model.Estoque;
import com.projeto.raizesnordeste.domain.model.MovimentacaoEstoque;
import com.projeto.raizesnordeste.presentation.api.IEstoqueController;
import com.projeto.raizesnordeste.presentation.mapper.EstoqueMapper;
import com.projeto.raizesnordeste.presentation.records.estoque.CreatedEstoqueRequest;
import com.projeto.raizesnordeste.presentation.records.estoque.EstoqueResponse;
import com.projeto.raizesnordeste.presentation.records.estoque.MovimentacaoEstoqueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/estoques")
@RequiredArgsConstructor
public class EstoqueController implements IEstoqueController {

    private final IEstoquePort useCase;
    private final EstoqueMapper mapper;

    @Override
    public ResponseEntity<EstoqueResponse> save(CreatedEstoqueRequest request) {
        Estoque estoque = mapper.toDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EstoqueResponse.from(useCase.save(estoque)));
    }

    @Override
    public ResponseEntity<EstoqueResponse> findById(UUID id) {
        return ResponseEntity.ok(EstoqueResponse.from(useCase.findById(id)));
    }

    @Override
    public ResponseEntity<Page<EstoqueResponse>> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        return ResponseEntity.ok(useCase.findAll(page, linesPerPage, direction, orderBy)
                .map(EstoqueResponse::from));
    }

    @Override
    public ResponseEntity<EstoqueResponse> findByUnidadeAndProduto(UUID idUnidade, UUID idProduto) {
        return ResponseEntity.ok(EstoqueResponse.from(useCase.findByUnidadeAndProduto(idUnidade, idProduto)));
    }

    @Override
    public ResponseEntity<EstoqueResponse> movimentar(MovimentacaoEstoqueRequest request) {
        MovimentacaoEstoque movimentacao = mapper.toDomain(request);
        return ResponseEntity.ok(EstoqueResponse.from(useCase.movimentar(movimentacao)));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
