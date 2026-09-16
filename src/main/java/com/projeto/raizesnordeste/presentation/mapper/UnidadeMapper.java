package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.Unidade;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.UnidadeEntity;
import com.projeto.raizesnordeste.presentation.records.CreatedUnidadeRequest;
import com.projeto.raizesnordeste.presentation.records.UnidadeResponse;
import com.projeto.raizesnordeste.presentation.records.UpdateUnidadeRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UnidadeMapper {

    Unidade toDomain(CreatedUnidadeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    Unidade updateFromRequest(UpdateUnidadeRequest request, @MappingTarget Unidade unidade);

    Unidade toDomain(UnidadeEntity entity);
    UnidadeEntity toEntity(Unidade unidade);
    UnidadeResponse toResponse(Unidade unidade);

}
