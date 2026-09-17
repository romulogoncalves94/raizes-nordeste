package com.projeto.raizesnordeste.infrastructure.persistence.repository;

import com.projeto.raizesnordeste.infrastructure.persistence.entities.CampanhasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ICampanhaRepository extends JpaRepository<CampanhasEntity, UUID> {

    @Query("SELECT c FROM CampanhasEntity c WHERE c.ativa = true AND c.dataInicio <= :agora AND c.dataFim >= :agora ORDER BY c.percentualDesconto DESC")
    List<CampanhasEntity> findVigentes(@Param("agora") LocalDateTime agora);
}
