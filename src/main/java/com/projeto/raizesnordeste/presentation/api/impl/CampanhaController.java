package com.projeto.raizesnordeste.presentation.api.impl;

import com.projeto.raizesnordeste.application.ports.ICampanhaPort;
import com.projeto.raizesnordeste.domain.model.Campanha;
import com.projeto.raizesnordeste.presentation.api.ICampanhaController;
import com.projeto.raizesnordeste.presentation.mapper.CampanhaMapper;
import com.projeto.raizesnordeste.presentation.records.campanha.CampanhaResponse;
import com.projeto.raizesnordeste.presentation.records.campanha.CreatedCampanhaRequest;
import com.projeto.raizesnordeste.presentation.records.campanha.UpdateCampanhaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
public class CampanhaController implements ICampanhaController {

    private final ICampanhaPort useCase;
    private final CampanhaMapper mapper;

    @Override
    public ResponseEntity<CampanhaResponse> save(CreatedCampanhaRequest request) {
        Campanha campanha = mapper.toDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CampanhaResponse.from(useCase.save(campanha)));
    }

    @Override
    public ResponseEntity<CampanhaResponse> findById(UUID id) {
        return ResponseEntity.ok(CampanhaResponse.from(useCase.findById(id)));
    }

    @Override
    public ResponseEntity<Page<CampanhaResponse>> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        return ResponseEntity.ok(useCase.findAll(page, linesPerPage, direction, orderBy)
                .map(CampanhaResponse::from));
    }

    @Override
    public ResponseEntity<List<CampanhaResponse>> findCampanhasVigentes() {
        return ResponseEntity.ok(useCase.findCampanhasVigentes().stream().map(CampanhaResponse::from).toList());
    }

    @Override
    public ResponseEntity<CampanhaResponse> update(UUID id, UpdateCampanhaRequest request) {
        Campanha campanha = useCase.findById(id);
        Campanha campanhaAtualizada = mapper.updateFromRequest(request, campanha);
        return ResponseEntity.ok(CampanhaResponse.from(useCase.update(id, campanhaAtualizada)));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
