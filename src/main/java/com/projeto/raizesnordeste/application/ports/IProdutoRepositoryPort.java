package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IProdutoRepositoryPort {
    Produto save(Produto produto);
    Optional<Produto> findById(UUID id);
    Page<Produto> findAll(Pageable pageable);
    Produto update(Produto produto);
    void deleteById(UUID id);
}
