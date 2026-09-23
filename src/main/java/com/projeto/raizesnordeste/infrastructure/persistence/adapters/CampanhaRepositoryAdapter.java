package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.ICampanhaRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Campanha;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.ICampanhaRepository;
import com.projeto.raizesnordeste.presentation.mapper.CampanhaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampanhaRepositoryAdapter implements ICampanhaRepositoryPort {

    private final ICampanhaRepository repository;
    private final CampanhaMapper mapper;

    @Override
    public Campanha save(Campanha campanha) {
        return mapper.toDomain(repository.save(mapper.toEntity(campanha)));
    }

    @Override
    public Optional<Campanha> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Campanha> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Campanha> findCampanhasVigentes(LocalDateTime data) {
        return repository.findVigentes(data).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Campanha update(Campanha campanha) {
        return mapper.toDomain(repository.save(mapper.toEntity(campanha)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
