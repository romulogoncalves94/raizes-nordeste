package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.domain.model.Unidade;
import com.projeto.raizesnordeste.presentation.mapper.UnidadeMapper;
import com.projeto.raizesnordeste.presentation.records.CreatedUnidadeRequest;
import com.projeto.raizesnordeste.presentation.records.UnidadeResponse;
import com.projeto.raizesnordeste.presentation.records.UpdateUnidadeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadeController implements IUnidadeController {

    private final IUnidadePort useCase;
    private final UnidadeMapper mapper;

    @Override
    public ResponseEntity<UnidadeResponse> save(CreatedUnidadeRequest request) {
        Unidade unidade = mapper.toDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UnidadeResponse.from(useCase.save(unidade)));
    }

    @Override
    public ResponseEntity<UnidadeResponse> findById(UUID id) {
        return ResponseEntity.ok(UnidadeResponse.from(useCase.findById(id)));
    }

    @Override
    public ResponseEntity<Page<UnidadeResponse>> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        return ResponseEntity.ok(useCase.findAll(page, linesPerPage, direction, orderBy)
                .map(UnidadeResponse::from));
    }

    @Override
    public ResponseEntity<UnidadeResponse> update(UUID id, UpdateUnidadeRequest request) {
        Unidade unidade = useCase.findById(id);
        Unidade unidadeAtualizada = mapper.updateFromRequest(request, unidade);
        return ResponseEntity.ok(UnidadeResponse.from(useCase.update(id, unidadeAtualizada)));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
