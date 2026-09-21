package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.Usuario;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.UsuarioEntity;
import com.projeto.raizesnordeste.presentation.records.usuario.CreatedUsuarioRequest;
import com.projeto.raizesnordeste.presentation.records.usuario.UpdateUsuarioRequest;
import com.projeto.raizesnordeste.presentation.records.usuario.UsuarioResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    Usuario toDomain(CreatedUsuarioRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    Usuario updateFromRequest(UpdateUsuarioRequest request, @MappingTarget Usuario usuario);

    Usuario toDomain(UsuarioEntity entity);
    UsuarioEntity toEntity(Usuario usuario);
    UsuarioResponse toResponse(Usuario usuario);

}
