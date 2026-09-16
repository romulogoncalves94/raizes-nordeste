package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Produto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IProdutoPort {
    Produto save(Produto produto);
    Produto findById(UUID id);
    Page<Produto> findAll(Integer page, Integer linesPerPage, String direction, String orderBy);
    Produto update(UUID id, Produto produto);
    void delete(UUID id);
}
