package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IProdutoRepositoryPort;
import com.projeto.raizesnordeste.domain.enums.CategoriaProdutoEnum;
import com.projeto.raizesnordeste.domain.model.Produto;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService")
class ProdutoServiceTest {

    @Mock
    private IProdutoRepositoryPort repositoryPort;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto getProduto(UUID id) {
        return new Produto(id, "Baião de Dois", new BigDecimal("29.90"), CategoriaProdutoEnum.PRATO_PRINCIPAL);
    }

    @Test
    @DisplayName("Deve salvar produto")
    void deveSalvarProduto() {
        Produto produto = getProduto(null);
        Produto salvo = getProduto(UUID.randomUUID());
        when(repositoryPort.save(produto)).thenReturn(salvo);

        Produto resultado = produtoService.save(produto);

        assertThat(resultado.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando produto não é encontrado por id")
    void deveLancarResourceNotFoundException_quandoProdutoNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve retornar produto quando encontrado por id")
    void deveRetornarProduto_quandoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        Produto produto = getProduto(id);
        when(repositoryPort.findById(id)).thenReturn(Optional.of(produto));

        Produto encontrado = produtoService.findById(id);

        assertThat(encontrado).isEqualTo(produto);
    }

    @Test
    @DisplayName("Deve atualizar produto")
    void deveAtualizarProduto() {
        UUID id = UUID.randomUUID();
        Produto produto = getProduto(id);
        when(repositoryPort.update(produto)).thenReturn(produto);

        Produto atualizado = produtoService.update(id, produto);

        assertThat(atualizado).isEqualTo(produto);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao excluir produto inexistente")
    void deveLancarResourceNotFoundException_quandoExcluirProdutoInexistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve excluir produto quando existente")
    void deveExcluirProduto_quandoExistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.of(getProduto(id)));

        produtoService.delete(id);

        verify(repositoryPort).deleteById(id);
    }
}
