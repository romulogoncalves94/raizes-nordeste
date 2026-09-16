package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.application.ports.IUnidadeRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Unidade;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.nonNull;

public class UnidadeService implements IUnidadePort {

    private final IUnidadeRepositoryPort repositoryPort;

    public UnidadeService(IUnidadeRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public Unidade save(Unidade unidade) {
        validarCnpjDuplicado(unidade.getCnpj());

        return repositoryPort.save(unidade);
    }

    @Override
    @Transactional(readOnly = true)
    public Unidade findById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Unidade> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
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
    public Unidade update(UUID id, Unidade unidade) {
        if (nonNull(unidade.getCnpj()) && repositoryPort.existsByCnpj(unidade.getCnpj(), id)) {
            throw new BusinessRuleException("CNPJ já cadastrado: " + unidade.getCnpj());
        }

        return repositoryPort.update(unidade);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findById(id);
        repositoryPort.deleteById(id);
    }

    private void validarCnpjDuplicado(String cnpj) {
        if (repositoryPort.existsByCnpj(cnpj, null)) {
            throw new BusinessRuleException("CNPJ já cadastrado: " + cnpj);
        }
    }
}
