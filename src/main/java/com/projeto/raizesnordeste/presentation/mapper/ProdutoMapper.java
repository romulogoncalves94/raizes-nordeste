package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.Produto;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.ProdutoEntity;
import com.projeto.raizesnordeste.presentation.records.produto.CreatedProdutoRequest;
import com.projeto.raizesnordeste.presentation.records.produto.ProdutoResponse;
import com.projeto.raizesnordeste.presentation.records.produto.UpdateProdutoRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    Produto toDomain(CreatedProdutoRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    Produto updateFromRequest(UpdateProdutoRequest request, @MappingTarget Produto produto);

    Produto toDomain(ProdutoEntity entity);
    ProdutoEntity toEntity(Produto produto);
    ProdutoResponse toResponse(Produto produto);

}
