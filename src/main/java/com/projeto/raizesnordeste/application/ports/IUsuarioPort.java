package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Usuario;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IUsuarioPort {
    Usuario save(Usuario usuario);
    Usuario findById(UUID id);
    Page<Usuario> findAll(Integer page, Integer linesPerPage, String direction, String orderBy);
    Usuario update(UUID id, Usuario usuario);
    void delete(UUID id);
}
