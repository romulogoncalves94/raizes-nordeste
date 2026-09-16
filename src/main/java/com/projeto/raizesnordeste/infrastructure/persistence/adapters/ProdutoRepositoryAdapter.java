package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.IProdutoRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Produto;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IProdutoRepository;
import com.projeto.raizesnordeste.presentation.mapper.ProdutoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoRepositoryAdapter implements IProdutoRepositoryPort {

    private final IProdutoRepository repository;
    private final ProdutoMapper mapper;

    @Override
    public Produto save(Produto produto) {
        return mapper.toDomain(repository.save(mapper.toEntity(produto)));
    }

    @Override
    public Optional<Produto> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Produto> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Produto update(Produto produto) {
        return mapper.toDomain(repository.save(mapper.toEntity(produto)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
