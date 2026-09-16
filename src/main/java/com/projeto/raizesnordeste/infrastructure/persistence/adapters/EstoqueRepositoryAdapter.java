package com.projeto.raizesnordeste.infrastructure.persistence.adapters;

import com.projeto.raizesnordeste.application.ports.IEstoqueRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Estoque;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.EstoqueEntity;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IEstoqueRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IProdutoRepository;
import com.projeto.raizesnordeste.infrastructure.persistence.repository.IUnidadeRepository;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import com.projeto.raizesnordeste.presentation.mapper.EstoqueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueRepositoryAdapter implements IEstoqueRepositoryPort {

    private final IEstoqueRepository repository;
    private final IUnidadeRepository unidadeRepository;
    private final IProdutoRepository produtoRepository;
    private final EstoqueMapper mapper;

    @Override
    public Estoque save(Estoque estoque) {
        EstoqueEntity entity = new EstoqueEntity();
        entity.setUnidade(unidadeRepository.getReferenceById(estoque.getIdUnidade()));
        entity.setProduto(produtoRepository.getReferenceById(estoque.getIdProduto()));
        entity.setQuantidade(estoque.getQuantidade());

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Estoque> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Estoque> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Estoque> findByUnidadeAndProduto(UUID idUnidade, UUID idProduto) {
        return repository.findByUnidade_IdAndProduto_Id(idUnidade, idProduto).map(mapper::toDomain);
    }

    @Override
    public Optional<Estoque> findByUnidadeAndProdutoParaAtualizacao(UUID idUnidade, UUID idProduto) {
        return repository.lockByUnidadeAndProduto(idUnidade, idProduto).map(mapper::toDomain);
    }

    @Override
    public Estoque update(Estoque estoque) {
        EstoqueEntity entity = repository.findById(estoque.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estoque não encontrado"));

        entity.setQuantidade(estoque.getQuantidade());

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByUnidadeAndProduto(UUID idUnidade, UUID idProduto) {
        return repository.findByUnidade_IdAndProduto_Id(idUnidade, idProduto).isPresent();
    }
}
