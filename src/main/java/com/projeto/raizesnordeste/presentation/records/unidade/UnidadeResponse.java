package com.projeto.raizesnordeste.presentation.records.unidade;

import com.projeto.raizesnordeste.domain.model.Unidade;

import java.util.UUID;

public record UnidadeResponse(
        UUID id,
        String razaoSocial,
        String cnpj,
        String cep,
        String logradouro,
        String bairro,
        String uf
) {
    public static UnidadeResponse from(Unidade unidade) {
        return new UnidadeResponse(
                unidade.getId(),
                unidade.getRazaoSocial(),
                unidade.getCnpj(),
                unidade.getCep(),
                unidade.getLogradouro(),
                unidade.getBairro(),
                unidade.getUf()
        );
    }
}
