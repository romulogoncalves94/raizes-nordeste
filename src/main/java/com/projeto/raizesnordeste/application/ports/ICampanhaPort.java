package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Campanha;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICampanhaPort {
    Campanha save(Campanha campanha);
    Campanha findById(UUID id);
    Page<Campanha> findAll(Integer page, Integer linesPerPage, String direction, String orderBy);
    List<Campanha> findVigentes();
    Optional<Campanha> findMelhorVigente();
    Campanha update(UUID id, Campanha campanha);
    void delete(UUID id);
}
