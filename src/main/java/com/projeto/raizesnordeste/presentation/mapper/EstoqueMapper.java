package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.Estoque;
import com.projeto.raizesnordeste.domain.model.MovimentacaoEstoque;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.EstoqueEntity;
import com.projeto.raizesnordeste.presentation.records.CreatedEstoqueRequest;
import com.projeto.raizesnordeste.presentation.records.MovimentacaoEstoqueRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EstoqueMapper {

    Estoque toDomain(CreatedEstoqueRequest request);

    MovimentacaoEstoque toDomain(MovimentacaoEstoqueRequest request);

    @Mapping(source = "unidade.id", target = "idUnidade")
    @Mapping(source = "unidade.razaoSocial", target = "nomeUnidade")
    @Mapping(source = "produto.id", target = "idProduto")
    @Mapping(source = "produto.nome", target = "nomeProduto")
    Estoque toDomain(EstoqueEntity entity);

}
