package com.projeto.raizesnordeste.infrastructure.persistence.entities;

import com.projeto.raizesnordeste.domain.enums.CategoriaProdutoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "produtos")
public class ProdutoEntity extends AuditoriaEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "NOME", nullable = false, length = 150)
    private String nome;

    @Column(name = "PRECO", nullable = false, length = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORIA", nullable = false, length = 30)
    private CategoriaProdutoEnum categoria;

}
