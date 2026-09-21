package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.ItemPedido;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.ItemPedidoEntity;
import com.projeto.raizesnordeste.presentation.records.pedido.CreatedItemPedidoRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemPedidoMapper {

    ItemPedido toDomain(CreatedItemPedidoRequest request);

    @Mapping(source = "produto.id", target = "idProduto")
    @Mapping(source = "produto.nome", target = "nomeProduto")
    ItemPedido toDomain(ItemPedidoEntity entity);

}
