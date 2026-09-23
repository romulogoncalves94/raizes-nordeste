package com.projeto.raizesnordeste.presentation.api.impl;

import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.domain.model.Usuario;
import com.projeto.raizesnordeste.presentation.api.IUsuarioController;
import com.projeto.raizesnordeste.presentation.mapper.UsuarioMapper;
import com.projeto.raizesnordeste.presentation.records.usuario.CreatedUsuarioRequest;
import com.projeto.raizesnordeste.presentation.records.usuario.UpdateUsuarioRequest;
import com.projeto.raizesnordeste.presentation.records.usuario.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController implements IUsuarioController {

    private final IUsuarioPort useCase;
    private final UsuarioMapper mapper;

    @Override
    public ResponseEntity<UsuarioResponse> save(CreatedUsuarioRequest request) {
        Usuario usuario = mapper.toDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.from(useCase.save(usuario)));
    }

    @Override
    public ResponseEntity<UsuarioResponse> findById(UUID id) {
        return ResponseEntity.ok(UsuarioResponse.from(useCase.findById(id)));
    }

    @Override
    public ResponseEntity<Page<UsuarioResponse>> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        return ResponseEntity.ok(useCase.findAll(page, linesPerPage, direction, orderBy)
                .map(UsuarioResponse::from));
    }

    @Override
    public ResponseEntity<UsuarioResponse> update(UUID id, UpdateUsuarioRequest request) {
        Usuario usuario = useCase.findById(id);
        Usuario usuarioAtualizado = mapper.updateFromRequest(request, usuario);
        boolean senhaAlterada = nonNull(request.senha());
        return ResponseEntity.ok(UsuarioResponse.from(useCase.update(id, usuarioAtualizado, senhaAlterada)));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}