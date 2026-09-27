package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.application.ports.IProdutoRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Produto;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
public class ProdutoService implements IProdutoPort {

    private final IProdutoRepositoryPort repositoryPort;

    public ProdutoService(IProdutoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public Produto save(Produto produto) {
        log.info("Cadastrando produto '{}' (categoria={}, preço=R$ {})", produto.getNome(), produto.getCategoria(), produto.getPreco());

        Produto produtoSalvo = repositoryPort.save(produto);
        log.info("Produto {} cadastrado: '{}' (categoria={}, preço=R$ {})",
                produtoSalvo.getId(), produtoSalvo.getNome(), produtoSalvo.getCategoria(), produtoSalvo.getPreco());

        return produtoSalvo;
    }

    @Override
    @Transactional(readOnly = true)
    public Produto findById(UUID id) {
        log.debug("Buscando produto {}", id);

        Produto produto = repositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.info("Produto {} não encontrado", id);
                    return new ResourceNotFoundException("Produto não encontrado");
                });

        log.debug("Produto {} encontrado ('{}')", produto.getId(), produto.getNome());
        return produto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Produto> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        log.debug("Listando produtos: page={} linesPerPage={} direction={} orderBy={}", page, linesPerPage, direction, orderBy);

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
        log.info("Atualizando produto {}", id);

        Produto produtoAtualizado = repositoryPort.update(produto);
        log.info("Produto {} atualizado: '{}' (categoria={}, preço=R$ {})",
                id, produtoAtualizado.getNome(), produtoAtualizado.getCategoria(), produtoAtualizado.getPreco());

        return produtoAtualizado;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Solicitada remoção do produto {}", id);

        findById(id);
        repositoryPort.deleteById(id);
        log.info("Produto {} removido", id);
    }
}
