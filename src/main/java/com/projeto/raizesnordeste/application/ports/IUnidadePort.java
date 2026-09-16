package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Unidade;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IUnidadePort {
    Unidade save(Unidade unidade);
    Unidade findById(UUID id);
    Page<Unidade> findAll(Integer page, Integer linesPerPage, String direction, String orderBy);
    Unidade update(UUID id, Unidade unidade);
    void delete(UUID id);
}
