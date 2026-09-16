package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Estoque;
import com.projeto.raizesnordeste.domain.model.MovimentacaoEstoque;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IEstoquePort {
    Estoque save(Estoque estoque);
    Estoque findById(UUID id);
    Page<Estoque> findAll(Integer page, Integer linesPerPage, String direction, String orderBy);
    Estoque findByUnidadeAndProduto(UUID idUnidade, UUID idProduto);
    Estoque movimentar(MovimentacaoEstoque movimentacao);
    void delete(UUID id);
}
