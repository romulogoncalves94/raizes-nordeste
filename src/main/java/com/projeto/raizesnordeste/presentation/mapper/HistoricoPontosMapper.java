package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.HistoricoPontos;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.HistoricoPontosEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoricoPontosMapper {

    @Mapping(source = "programaFidelidade.id", target = "idProgramaFidelidade")
    @Mapping(source = "criadoEm", target = "registradoEm")
    HistoricoPontos toDomain(HistoricoPontosEntity entity);

}
