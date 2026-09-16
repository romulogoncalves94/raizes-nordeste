package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.infrastructure.persistence.entities.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IProdutoRepository extends JpaRepository<ProdutoEntity, UUID> {
}
