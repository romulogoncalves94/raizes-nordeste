package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.HistoricoPontos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IHistoricoPontosRepositoryPort {
    HistoricoPontos save(HistoricoPontos historico);
    Page<HistoricoPontos> findByProgramaFidelidadeId(UUID idProgramaFidelidade, Pageable pageable);
}
