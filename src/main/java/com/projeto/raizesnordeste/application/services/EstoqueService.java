package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IEstoquePort;
import com.projeto.raizesnordeste.application.ports.IEstoqueRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.domain.enums.TipoMovimentacaoEstoqueEnum;
import com.projeto.raizesnordeste.domain.model.Estoque;
import com.projeto.raizesnordeste.domain.model.MovimentacaoEstoque;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class EstoqueService implements IEstoquePort {

    private final IEstoqueRepositoryPort repositoryPort;
    private final IUnidadePort unidadePort;
    private final IProdutoPort produtoPort;

    public EstoqueService(IEstoqueRepositoryPort repositoryPort, IUnidadePort unidadePort, IProdutoPort produtoPort) {
        this.repositoryPort = repositoryPort;
        this.unidadePort = unidadePort;
        this.produtoPort = produtoPort;
    }

    @Override
    @Transactional
    public Estoque save(Estoque estoque) {
        unidadePort.findById(estoque.getIdUnidade());
        produtoPort.findById(estoque.getIdProduto());

        if (repositoryPort.existsByUnidadeAndProduto(estoque.getIdUnidade(), estoque.getIdProduto())) {
            throw new BusinessRuleException("Já existe registro de estoque para esta unidade e produto");
        }

        return repositoryPort.save(estoque);
    }

    @Override
    @Transactional(readOnly = true)
    public Estoque findById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Estoque> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        PageRequest pageRequest = PageRequest.of(
                page,
                linesPerPage,
                Sort.Direction.valueOf(direction),
                orderBy
        );

        return repositoryPort.findAll(pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Estoque findByUnidadeAndProduto(UUID idUnidade, UUID idProduto) {
        return repositoryPort.findByUnidadeAndProduto(idUnidade, idProduto)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado para a unidade e produto informados"));
    }

    @Override
    @Transactional
    public Estoque movimentar(MovimentacaoEstoque movimentacao) {
        Estoque estoque = repositoryPort.findByUnidadeAndProdutoParaAtualizacao(movimentacao.getIdUnidade(), movimentacao.getIdProduto())
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado para a unidade e produto informados"));

        Integer quantidade = movimentacao.getQuantidade();

        if (TipoMovimentacaoEstoqueEnum.SAIDA.equals(movimentacao.getTipo())) {
            if (estoque.getQuantidade() < quantidade) {
                throw new BusinessRuleException(
                        String.format("Quantidade insuficiente em estoque. Disponível: %d, solicitado: %d", estoque.getQuantidade(), quantidade)
                );
            }
            estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        } else {
            estoque.setQuantidade(estoque.getQuantidade() + quantidade);
        }

        return repositoryPort.update(estoque);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findById(id);
        repositoryPort.deleteById(id);
    }
}
