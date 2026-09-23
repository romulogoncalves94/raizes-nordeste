package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Campanha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICampanhaRepositoryPort {
    Campanha save(Campanha campanha);
    Optional<Campanha> findById(UUID id);
    Page<Campanha> findAll(Pageable pageable);
    List<Campanha> findCampanhasVigentes(LocalDateTime data);
    Campanha update(Campanha campanha);
    void deleteById(UUID id);
}
