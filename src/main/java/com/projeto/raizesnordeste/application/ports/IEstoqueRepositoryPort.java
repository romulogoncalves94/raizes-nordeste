package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Estoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IEstoqueRepositoryPort {
    Estoque save(Estoque estoque);
    Optional<Estoque> findById(UUID id);
    Page<Estoque> findAll(Pageable pageable);
    Optional<Estoque> findByUnidadeAndProduto(UUID idUnidade, UUID idProduto);
    Optional<Estoque> findByUnidadeAndProdutoParaAtualizacao(UUID idUnidade, UUID idProduto);
    Estoque update(Estoque estoque);
    void deleteById(UUID id);
    boolean existsByUnidadeAndProduto(UUID idUnidade, UUID idProduto);
}
