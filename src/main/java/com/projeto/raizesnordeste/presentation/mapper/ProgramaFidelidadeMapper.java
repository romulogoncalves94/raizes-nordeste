package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.ProgramaFidelidade;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.ProgramaFidelidadeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProgramaFidelidadeMapper {

    @Mapping(source = "usuario.id", target = "idUsuario")
    @Mapping(source = "usuario.nome", target = "nomeUsuario")
    ProgramaFidelidade toDomain(ProgramaFidelidadeEntity entity);

}
