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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
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
        log.info("Criando registro de estoque: unidade={}, produto={}, quantidade inicial={}",
                estoque.getIdUnidade(), estoque.getIdProduto(), estoque.getQuantidade());

        unidadePort.findById(estoque.getIdUnidade());
        produtoPort.findById(estoque.getIdProduto());

        if (repositoryPort.existsByUnidadeAndProduto(estoque.getIdUnidade(), estoque.getIdProduto())) {
            log.warn("Tentativa de criar estoque duplicado para unidade={} produto={}", estoque.getIdUnidade(), estoque.getIdProduto());
            throw new BusinessRuleException("Já existe registro de estoque para esta unidade e produto");
        }

        Estoque estoqueSalvo = repositoryPort.save(estoque);
        log.info("Estoque {} criado com sucesso", estoqueSalvo.getId());

        return estoqueSalvo;
    }

    @Override
    @Transactional(readOnly = true)
    public Estoque findById(UUID id) {
        log.debug("Buscando estoque {}", id);

        Estoque estoque = repositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.info("Estoque {} não encontrado", id);
                    return new ResourceNotFoundException("Estoque não encontrado");
                });

        log.debug("Estoque {} encontrado (quantidade={})", estoque.getId(), estoque.getQuantidade());
        return estoque;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Estoque> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        log.debug("Listando estoques: page={} linesPerPage={} direction={} orderBy={}", page, linesPerPage, direction, orderBy);

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
        log.debug("Buscando estoque por unidade={} produto={}", idUnidade, idProduto);

        Estoque estoque = repositoryPort.findByUnidadeAndProduto(idUnidade, idProduto)
                .orElseThrow(() -> {
                    log.info("Estoque não encontrado para unidade={} produto={}", idUnidade, idProduto);
                    return new ResourceNotFoundException("Estoque não encontrado para a unidade e produto informados");
                });

        log.debug("Estoque {} encontrado para unidade={} produto={}", estoque.getId(), idUnidade, idProduto);
        return estoque;
    }

    @Override
    @Transactional
    public Estoque movimentar(MovimentacaoEstoque movimentacao) {
        log.info("Movimentando estoque: unidade={} produto={} tipo={} quantidade={}",
                movimentacao.getIdUnidade(), movimentacao.getIdProduto(), movimentacao.getTipo(), movimentacao.getQuantidade());

        Estoque estoque = repositoryPort.findByUnidadeAndProdutoParaAtualizacao(movimentacao.getIdUnidade(), movimentacao.getIdProduto())
                .orElseThrow(() -> {
                    log.warn("Movimentação recusada: estoque não encontrado para unidade={} produto={}",
                            movimentacao.getIdUnidade(), movimentacao.getIdProduto());
                    return new ResourceNotFoundException("Estoque não encontrado para a unidade e produto informados");
                });

        Integer quantidade = movimentacao.getQuantidade();
        Integer quantidadeAnterior = estoque.getQuantidade();

        if (TipoMovimentacaoEstoqueEnum.SAIDA.equals(movimentacao.getTipo())) {
            if (estoque.getQuantidade() < quantidade) {
                log.warn("Estoque insuficiente para saída: unidade={} produto={} disponível={} solicitado={}",
                        movimentacao.getIdUnidade(), movimentacao.getIdProduto(), estoque.getQuantidade(), quantidade);
                throw new BusinessRuleException(
                        String.format("Quantidade insuficiente em estoque. Disponível: %d, solicitado: %d", estoque.getQuantidade(), quantidade)
                );
            }
            estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        } else {
            estoque.setQuantidade(estoque.getQuantidade() + quantidade);
        }

        Estoque estoqueAtualizado = repositoryPort.update(estoque);
        log.info("Estoque {} movimentado ({}): unidade={} produto={} {} -> {}",
                estoque.getId(), movimentacao.getTipo(), movimentacao.getIdUnidade(), movimentacao.getIdProduto(),
                quantidadeAnterior, estoqueAtualizado.getQuantidade());

        return estoqueAtualizado;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Solicitada remoção do estoque {}", id);

        findById(id);
        repositoryPort.deleteById(id);
        log.info("Estoque {} removido", id);
    }
}
