package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.IHistoricoPontosRepositoryPort;
import com.projeto.raizesnordeste.domain.model.HistoricoPontos;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.HistoricoPontosEntity;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IHistoricoPontosRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IProgramaFidelidadeRepository;
import com.projeto.raizesnordeste.presentation.mapper.HistoricoPontosMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoricoPontosRepositoryAdapter implements IHistoricoPontosRepositoryPort {

    private final IHistoricoPontosRepository repository;
    private final IProgramaFidelidadeRepository programaFidelidadeRepository;
    private final HistoricoPontosMapper mapper;

    @Override
    public HistoricoPontos save(HistoricoPontos historico) {
        HistoricoPontosEntity entity = new HistoricoPontosEntity();
        entity.setProgramaFidelidade(programaFidelidadeRepository.getReferenceById(historico.getIdProgramaFidelidade()));
        entity.setPontos(historico.getPontos());
        entity.setTipoHistorico(historico.getTipoHistorico());

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Page<HistoricoPontos> findByProgramaFidelidadeId(UUID idProgramaFidelidade, Pageable pageable) {
        return repository.findByProgramaFidelidade_Id(idProgramaFidelidade, pageable)
                .map(mapper::toDomain);
    }
}
