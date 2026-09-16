package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IUsuarioRepositoryPort {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(UUID id);
    Optional<Usuario> findByEmail(String email);
    Page<Usuario> findAll(Pageable pageable);
    Usuario update(Usuario usuario);
    void deleteById(UUID id);
    boolean existsByCpf(String cpf, UUID id);
    boolean existsByEmail(String email, UUID id);
}
