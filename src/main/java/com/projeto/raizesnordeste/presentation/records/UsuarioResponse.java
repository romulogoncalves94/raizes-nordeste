package com.projeto.raizesnordeste.presentation.records;

import com.projeto.raizesnordeste.domain.enums.PerfilUsuarioEnum;
import com.projeto.raizesnordeste.domain.model.Usuario;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String cpf,
        String email,
        PerfilUsuarioEnum perfil,
        Boolean aceiteLgpd,
        Boolean aceiteFidelidade
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getAceiteLgpd(),
                usuario.getAceiteFidelidade()
        );
    }
}