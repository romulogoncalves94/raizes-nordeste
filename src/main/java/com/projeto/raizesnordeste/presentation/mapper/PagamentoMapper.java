package com.projeto.raizesnordeste.presentation.mapper;

import com.projeto.raizesnordeste.domain.model.Pagamento;
import com.projeto.raizesnordeste.domain.model.SolicitacaoPagamento;
import com.projeto.raizesnordeste.infrastructure.persistence.entities.PagamentoEntity;
import com.projeto.raizesnordeste.presentation.records.CreatedPagamentoRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagamentoMapper {

    SolicitacaoPagamento toDomain(CreatedPagamentoRequest request);

    @Mapping(source = "pedido.id", target = "idPedido")
    Pagamento toDomain(PagamentoEntity entity);

}
