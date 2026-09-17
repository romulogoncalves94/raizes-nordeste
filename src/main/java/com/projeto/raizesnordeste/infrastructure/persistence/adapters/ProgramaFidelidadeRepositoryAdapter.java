package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadeRepositoryPort;
import com.projeto.raizesnordeste.domain.model.ProgramaFidelidade;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.ProgramaFidelidadeEntity;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IProgramaFidelidadeRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IUsuarioRepository;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import com.projeto.raizesnordeste.presentation.mapper.ProgramaFidelidadeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgramaFidelidadeRepositoryAdapter implements IProgramaFidelidadeRepositoryPort {

    private final IProgramaFidelidadeRepository repository;
    private final IUsuarioRepository usuarioRepository;
    private final ProgramaFidelidadeMapper mapper;

    @Override
    public ProgramaFidelidade save(ProgramaFidelidade programa) {
        ProgramaFidelidadeEntity entity = new ProgramaFidelidadeEntity();
        entity.setUsuario(usuarioRepository.getReferenceById(programa.getIdUsuario()));
        entity.setSaldoPontos(programa.getSaldoPontos());

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<ProgramaFidelidade> findByUsuarioId(UUID idUsuario) {
        return repository.findByUsuario_Id(idUsuario).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsuarioId(UUID idUsuario) {
        return repository.existsByUsuario_Id(idUsuario);
    }

    @Override
    public ProgramaFidelidade update(ProgramaFidelidade programa) {
        ProgramaFidelidadeEntity entity = repository.findById(programa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Programa de fidelidade não encontrado"));

        entity.setSaldoPontos(programa.getSaldoPontos());

        return mapper.toDomain(repository.save(entity));
    }
}
