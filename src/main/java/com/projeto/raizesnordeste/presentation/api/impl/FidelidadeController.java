package com.projeto.raizesnordeste.presentation.api.impl;

import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadePort;
import com.projeto.raizesnordeste.presentation.api.IFidelidadeController;
import com.projeto.raizesnordeste.presentation.records.fidelidade.HistoricoPontosResponse;
import com.projeto.raizesnordeste.presentation.records.fidelidade.ProgramaFidelidadeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/fidelidade")
@RequiredArgsConstructor
public class FidelidadeController implements IFidelidadeController {

    private final IProgramaFidelidadePort useCase;

    @Override
    public ResponseEntity<ProgramaFidelidadeResponse> findByUsuario(UUID idUsuario) {
        return ResponseEntity.ok(ProgramaFidelidadeResponse.from(useCase.findByUsuario(idUsuario)));
    }

    @Override
    public ResponseEntity<Page<HistoricoPontosResponse>> findHistorico(UUID idUsuario, Integer page, Integer linesPerPage, String direction, String orderBy) {
        return ResponseEntity.ok(useCase.findHistorico(idUsuario, page, linesPerPage, direction, orderBy)
                .map(HistoricoPontosResponse::from));
    }
}
