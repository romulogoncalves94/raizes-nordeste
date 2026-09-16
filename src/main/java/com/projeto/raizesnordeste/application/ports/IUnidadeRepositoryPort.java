package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Unidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IUnidadeRepositoryPort {
    Unidade save(Unidade unidade);
    Optional<Unidade> findById(UUID id);
    Page<Unidade> findAll(Pageable pageable);
    Unidade update(Unidade unidade);
    void deleteById(UUID id);
    boolean existsByCnpj(String cnpj, UUID id);
}
