package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.application.ports.IUnidadeRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Unidade;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Slf4j
public class UnidadeService implements IUnidadePort {

    private final IUnidadeRepositoryPort repositoryPort;

    public UnidadeService(IUnidadeRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public Unidade save(Unidade unidade) {
        log.info("Cadastrando unidade '{}' (cnpj={})", unidade.getRazaoSocial(), unidade.getCnpj());

        validarCnpjDuplicado(unidade.getCnpj());

        Unidade unidadeSalva = repositoryPort.save(unidade);
        log.info("Unidade {} cadastrada: '{}' (cnpj={})", unidadeSalva.getId(), unidadeSalva.getRazaoSocial(), unidadeSalva.getCnpj());

        return unidadeSalva;
    }

    @Override
    @Transactional(readOnly = true)
    public Unidade findById(UUID id) {
        log.debug("Buscando unidade {}", id);

        Unidade unidade = repositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.info("Unidade {} não encontrada", id);
                    return new ResourceNotFoundException("Unidade não encontrada");
                });

        log.debug("Unidade {} encontrada ('{}')", unidade.getId(), unidade.getRazaoSocial());
        return unidade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Unidade> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        log.debug("Listando unidades: page={} linesPerPage={} direction={} orderBy={}", page, linesPerPage, direction, orderBy);

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
        log.info("Atualizando unidade {}", id);

        if (nonNull(unidade.getCnpj()) && repositoryPort.existsByCnpj(unidade.getCnpj(), id)) {
            log.warn("Tentativa de atualizar unidade {} com CNPJ já cadastrado", id);
            throw new BusinessRuleException("CNPJ já cadastrado: " + unidade.getCnpj());
        }

        Unidade unidadeAtualizada = repositoryPort.update(unidade);
        log.info("Unidade {} atualizada: '{}'", id, unidadeAtualizada.getRazaoSocial());

        return unidadeAtualizada;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Solicitada remoção da unidade {}", id);

        findById(id);
        repositoryPort.deleteById(id);
        log.info("Unidade {} removida", id);
    }

    private void validarCnpjDuplicado(String cnpj) {
        if (repositoryPort.existsByCnpj(cnpj, null)) {
            log.warn("Tentativa de cadastro de unidade com CNPJ já existente");
            throw new BusinessRuleException("CNPJ já cadastrado: " + cnpj);
        }
    }
}
