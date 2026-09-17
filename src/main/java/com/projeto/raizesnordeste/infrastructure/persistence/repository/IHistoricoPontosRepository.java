package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.infrastructure.persistence.entities.HistoricoPontosEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IHistoricoPontosRepository extends JpaRepository<HistoricoPontosEntity, UUID> {
    Page<HistoricoPontosEntity> findByProgramaFidelidade_Id(UUID idProgramaFidelidade, Pageable pageable);
}
