package com.projeto.raizesnordeste.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditoriaEntity {

    @CreationTimestamp
    @Column(name = "CRIADO_EM", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "CRIADO_POR", length = 150)
    private String criadoPor;

    @UpdateTimestamp
    @Column(name = "ALTERADO_EM")
    private LocalDateTime alteradoEm;

    @Column(name = "ALTERADO_POR", length = 150)
    private String alteradoPor;

}
