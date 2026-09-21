package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.Campanha;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.CampanhasEntity;
import com.projeto.raizesnordeste.presentation.records.campanha.CreatedCampanhaRequest;
import com.projeto.raizesnordeste.presentation.records.campanha.UpdateCampanhaRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CampanhaMapper {

    Campanha toDomain(CreatedCampanhaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    Campanha updateFromRequest(UpdateCampanhaRequest request, @MappingTarget Campanha campanha);

    Campanha toDomain(CampanhasEntity entity);
    CampanhasEntity toEntity(Campanha campanha);

}
