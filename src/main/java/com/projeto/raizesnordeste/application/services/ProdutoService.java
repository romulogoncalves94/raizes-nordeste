package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.application.ports.IProdutoRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Produto;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ProdutoService implements IProdutoPort {

    private final IProdutoRepositoryPort repositoryPort;

    public ProdutoService(IProdutoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public Produto save(Produto produto) {
        return repositoryPort.save(produto);
    }

    @Override
    @Transactional(readOnly = true)
    public Produto findById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Produto> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        PageRequest pageRequest = PageRequest.of(
                page,
                linesPerPage,
                Sort.Direction.valueOf(direction),
                orderBy
        );

        return repositoryPort.findAll(pageRequest);
    }

    @Override
    @Transactional
    public Produto update(UUID id, Produto produto) {
        return repositoryPort.update(produto);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findById(id);
        repositoryPort.deleteById(id);
    }
}
