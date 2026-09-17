package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.Pedido;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.PedidoEntity;
import com.projeto.raizesnordeste.presentation.records.CreatedPedidoRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ItemPedidoMapper.class)
public interface PedidoMapper {

    Pedido toDomain(CreatedPedidoRequest request);

    @Mapping(source = "usuario.id", target = "idUsuario")
    @Mapping(source = "usuario.nome", target = "nomeUsuario")
    @Mapping(source = "unidade.id", target = "idUnidade")
    @Mapping(source = "unidade.razaoSocial", target = "nomeUnidade")
    @Mapping(source = "campanhaAplicada.id", target = "idCampanhaAplicada")
    @Mapping(source = "campanhaAplicada.nome", target = "nomeCampanhaAplicada")
    Pedido toDomain(PedidoEntity entity);

}
