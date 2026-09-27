package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.ICampanhaPort;
import com.projeto.raizesnordeste.application.ports.ICampanhaRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Campanha;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class CampanhaService implements ICampanhaPort {

    private final ICampanhaRepositoryPort repositoryPort;

    public CampanhaService(ICampanhaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public Campanha save(Campanha campanha) {
        log.info("Criando campanha '{}' ({}% de desconto, vigência {} a {})",
                campanha.getNome(), campanha.getPercentualDesconto(), campanha.getDataInicio(), campanha.getDataFim());

        validarVigencia(campanha);

        if (isNull(campanha.getAtiva())) {
            campanha.setAtiva(true);
        }

        Campanha campanhaSalva = repositoryPort.save(campanha);
        log.info("Campanha {} criada: '{}' ({}% de desconto, vigência {} a {}, ativa={})",
                campanhaSalva.getId(), campanhaSalva.getNome(), campanhaSalva.getPercentualDesconto(),
                campanhaSalva.getDataInicio(), campanhaSalva.getDataFim(), campanhaSalva.getAtiva());

        return campanhaSalva;
    }

    @Override
    @Transactional(readOnly = true)
    public Campanha findById(UUID id) {
        log.debug("Buscando campanha {}", id);

        Campanha campanha = repositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.info("Campanha {} não encontrada", id);
                    return new ResourceNotFoundException("Campanha não encontrada");
                });

        log.debug("Campanha {} encontrada ('{}')", campanha.getId(), campanha.getNome());
        return campanha;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Campanha> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        log.debug("Listando campanhas: page={} linesPerPage={} direction={} orderBy={}", page, linesPerPage, direction, orderBy);

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
    public List<Campanha> findCampanhasVigentes() {
        log.debug("Buscando campanhas vigentes");
        List<Campanha> campanhas = repositoryPort.findCampanhasVigentes(LocalDateTime.now());
        log.debug("{} campanha(s) vigente(s) encontrada(s)", campanhas.size());

        return campanhas;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Campanha> findMelhorVigente() {
        log.debug("Buscando melhor campanha vigente");

        Optional<Campanha> melhor = findCampanhasVigentes().stream()
                .max(Comparator.comparing(Campanha::getPercentualDesconto));

        melhor.ifPresentOrElse(
                campanha -> log.debug("Melhor campanha vigente: '{}' ({}%)", campanha.getNome(), campanha.getPercentualDesconto()),
                () -> log.debug("Nenhuma campanha vigente encontrada")
        );

        return melhor;
    }

    @Override
    @Transactional
    public Campanha update(UUID id, Campanha campanha) {
        log.info("Atualizando campanha {}", id);

        validarVigencia(campanha);

        Campanha campanhaAtualizada = repositoryPort.update(campanha);
        log.info("Campanha {} atualizada: '{}' ({}% de desconto, ativa={})",
                id, campanhaAtualizada.getNome(), campanhaAtualizada.getPercentualDesconto(), campanhaAtualizada.getAtiva());

        return campanhaAtualizada;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Solicitada remoção da campanha {}", id);

        findById(id);
        repositoryPort.deleteById(id);
        log.info("Campanha {} removida", id);
    }

    private void validarVigencia(Campanha campanha) {
        if (nonNull(campanha.getDataInicio()) && nonNull(campanha.getDataFim())
                && !campanha.getDataFim().isAfter(campanha.getDataInicio())) {
            log.warn("Vigência inválida para campanha '{}': dataInicio={} dataFim={}",
                    campanha.getNome(), campanha.getDataInicio(), campanha.getDataFim());
            throw new BusinessRuleException("A dataFim da campanha deve ser posterior à dataInicio");
        }
    }
}
