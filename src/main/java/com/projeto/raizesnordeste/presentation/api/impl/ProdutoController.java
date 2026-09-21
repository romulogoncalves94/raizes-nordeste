package com.projeto.raizesnordeste.presentation.api.impl;

import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.domain.model.Produto;
import com.projeto.raizesnordeste.presentation.api.IProdutoController;
import com.projeto.raizesnordeste.presentation.mapper.ProdutoMapper;
import com.projeto.raizesnordeste.presentation.records.produto.CreatedProdutoRequest;
import com.projeto.raizesnordeste.presentation.records.produto.ProdutoResponse;
import com.projeto.raizesnordeste.presentation.records.produto.UpdateProdutoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController implements IProdutoController {

    private final IProdutoPort useCase;
    private final ProdutoMapper mapper;

    @Override
    public ResponseEntity<ProdutoResponse> save(CreatedProdutoRequest request) {
        Produto produto = mapper.toDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProdutoResponse.from(useCase.save(produto)));
    }

    @Override
    public ResponseEntity<ProdutoResponse> findById(UUID id) {
        return ResponseEntity.ok(ProdutoResponse.from(useCase.findById(id)));
    }

    @Override
    public ResponseEntity<Page<ProdutoResponse>> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        return ResponseEntity.ok(useCase.findAll(page, linesPerPage, direction, orderBy)
                .map(ProdutoResponse::from));
    }

    @Override
    public ResponseEntity<ProdutoResponse> update(UUID id, UpdateProdutoRequest request) {
        Produto produto = useCase.findById(id);
        Produto produtoAtualizado = mapper.updateFromRequest(request, produto);
        return ResponseEntity.ok(ProdutoResponse.from(useCase.update(id, produtoAtualizado)));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
