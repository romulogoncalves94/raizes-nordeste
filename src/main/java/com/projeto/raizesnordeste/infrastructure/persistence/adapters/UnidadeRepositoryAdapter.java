package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.IUnidadeRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Unidade;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IUnidadeRepository;
import com.projeto.raizesnordeste.presentation.mapper.UnidadeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class UnidadeRepositoryAdapter implements IUnidadeRepositoryPort {

    private final IUnidadeRepository repository;
    private final UnidadeMapper mapper;

    @Override
    public Unidade save(Unidade unidade) {
        return mapper.toDomain(repository.save(mapper.toEntity(unidade)));
    }

    @Override
    public Optional<Unidade> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Unidade> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Unidade update(Unidade unidade) {
        return mapper.toDomain(repository.save(mapper.toEntity(unidade)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByCnpj(String cnpj, UUID id) {
        return repository.findByCnpj(cnpj)
                .filter(unidade -> isNull(id) || !unidade.getId().equals(id))
                .isPresent();
    }
}
