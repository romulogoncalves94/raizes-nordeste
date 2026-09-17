package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.ICampanhaPort;
import com.projeto.raizesnordeste.application.ports.ICampanhaRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Campanha;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CampanhaService implements ICampanhaPort {

    private final ICampanhaRepositoryPort repositoryPort;

    public CampanhaService(ICampanhaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public Campanha save(Campanha campanha) {
        validarVigencia(campanha);

        if (campanha.getAtiva() == null) {
            campanha.setAtiva(true);
        }

        return repositoryPort.save(campanha);
    }

    @Override
    @Transactional(readOnly = true)
    public Campanha findById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Campanha> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
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
    public List<Campanha> findVigentes() {
        return repositoryPort.findVigentes(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Campanha> findMelhorVigente() {
        return findVigentes().stream()
                .max(Comparator.comparing(Campanha::getPercentualDesconto));
    }

    @Override
    @Transactional
    public Campanha update(UUID id, Campanha campanha) {
        validarVigencia(campanha);

        return repositoryPort.update(campanha);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findById(id);
        repositoryPort.deleteById(id);
    }

    private void validarVigencia(Campanha campanha) {
        if (campanha.getDataInicio() != null && campanha.getDataFim() != null
                && !campanha.getDataFim().isAfter(campanha.getDataInicio())) {
            throw new BusinessRuleException("A dataFim da campanha deve ser posterior à dataInicio");
        }
    }
}
