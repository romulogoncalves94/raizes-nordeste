package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.infrastructure.persistence.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {
    Optional<UsuarioEntity> findByCpf(String cpf);
    Optional<UsuarioEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
