package com.projeto.raizesnordeste.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditoriaEntity {

    @CreationTimestamp
    @Column(name = "CRIADO_EM", updatable = false)
    private LocalDateTime criadoEm;

    @CreatedBy
    @Column(name = "CRIADO_POR", length = 150, updatable = false)
    private String criadoPor;

    @UpdateTimestamp
    @Column(name = "ALTERADO_EM")
    private LocalDateTime alteradoEm;

    @LastModifiedBy
    @Column(name = "ALTERADO_POR", length = 150)
    private String alteradoPor;

}
