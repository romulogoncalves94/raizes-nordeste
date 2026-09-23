package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IEstoqueRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.domain.enums.TipoMovimentacaoEstoqueEnum;
import com.projeto.raizesnordeste.domain.model.Estoque;
import com.projeto.raizesnordeste.domain.model.MovimentacaoEstoque;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueService")
class EstoqueServiceTest {

    @Mock
    private IEstoqueRepositoryPort repositoryPort;

    @Mock
    private IUnidadePort unidadePort;

    @Mock
    private IProdutoPort produtoPort;

    @InjectMocks
    private EstoqueService estoqueService;

    private Estoque getEstoque(UUID id, UUID idUnidade, UUID idProduto, Integer quantidade) {
        return new Estoque(id, idUnidade, "Unidade Centro", idProduto, "Baião de Dois", quantidade);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao salvar estoque duplicado para unidade e produto")
    void deveLancarBusinessRuleException_quandoSalvarEstoqueDuplicadoParaUnidadeEProduto() {
        UUID idUnidade = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        Estoque estoque = getEstoque(null, idUnidade, idProduto, 10);
        when(repositoryPort.existsByUnidadeAndProduto(idUnidade, idProduto)).thenReturn(true);

        assertThatThrownBy(() -> estoqueService.save(estoque))
                .isInstanceOf(BusinessRuleException.class);

        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar estoque quando unidade e produto existem e não há duplicidade")
    void deveSalvarEstoque_quandoUnidadeEProdutoExistemESemDuplicidade() {
        UUID idUnidade = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        Estoque estoque = getEstoque(null, idUnidade, idProduto, 10);
        when(repositoryPort.existsByUnidadeAndProduto(idUnidade, idProduto)).thenReturn(false);
        when(repositoryPort.save(estoque)).thenReturn(getEstoque(UUID.randomUUID(), idUnidade, idProduto, 10));

        Estoque estoqueSalvo = estoqueService.save(estoque);

        assertThat(estoqueSalvo.getId()).isNotNull();
        verify(unidadePort).findById(idUnidade);
        verify(produtoPort).findById(idProduto);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando estoque não é encontrado por id")
    void deveLancarResourceNotFoundException_quandoEstoqueNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estoqueService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar por unidade e produto inexistentes")
    void deveLancarResourceNotFoundException_quandoBuscarPorUnidadeEProdutoInexistente() {
        UUID idUnidade = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        when(repositoryPort.findByUnidadeAndProduto(idUnidade, idProduto)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estoqueService.findByUnidadeAndProduto(idUnidade, idProduto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve debitar estoque quando movimentação de saída com saldo suficiente")
    void deveDebitarEstoque_quandoMovimentacaoSaidaComSaldoSuficiente() {
        UUID idUnidade = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        Estoque estoque = getEstoque(UUID.randomUUID(), idUnidade, idProduto, 10);
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(idUnidade, idProduto, 4, TipoMovimentacaoEstoqueEnum.SAIDA);

        when(repositoryPort.findByUnidadeAndProdutoParaAtualizacao(idUnidade, idProduto)).thenReturn(Optional.of(estoque));
        when(repositoryPort.update(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Estoque atualizado = estoqueService.movimentar(movimentacao);

        assertThat(atualizado.getQuantidade()).isEqualTo(6);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando movimentação de saída com saldo insuficiente")
    void deveLancarBusinessRuleException_quandoMovimentacaoSaidaComSaldoInsuficiente() {
        UUID idUnidade = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        Estoque estoque = getEstoque(UUID.randomUUID(), idUnidade, idProduto, 3);
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(idUnidade, idProduto, 10, TipoMovimentacaoEstoqueEnum.SAIDA);

        when(repositoryPort.findByUnidadeAndProdutoParaAtualizacao(idUnidade, idProduto)).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> estoqueService.movimentar(movimentacao))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Disponível: 3")
                .hasMessageContaining("solicitado: 10");

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Deve incrementar estoque quando movimentação de entrada")
    void deveIncrementarEstoque_quandoMovimentacaoEntrada() {
        UUID idUnidade = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        Estoque estoque = getEstoque(UUID.randomUUID(), idUnidade, idProduto, 5);
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(idUnidade, idProduto, 7, TipoMovimentacaoEstoqueEnum.ENTRADA);

        when(repositoryPort.findByUnidadeAndProdutoParaAtualizacao(idUnidade, idProduto)).thenReturn(Optional.of(estoque));
        when(repositoryPort.update(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Estoque estoqueAtualizado = estoqueService.movimentar(movimentacao);

        assertThat(estoqueAtualizado.getQuantidade()).isEqualTo(12);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao movimentar estoque inexistente")
    void deveLancarResourceNotFoundException_quandoMovimentarEstoqueInexistente() {
        UUID idUnidade = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(idUnidade, idProduto, 1, TipoMovimentacaoEstoqueEnum.SAIDA);

        when(repositoryPort.findByUnidadeAndProdutoParaAtualizacao(idUnidade, idProduto)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estoqueService.movimentar(movimentacao))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao excluir estoque inexistente")
    void deveLancarResourceNotFoundException_quandoExcluirEstoqueInexistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estoqueService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve excluir estoque quando existente")
    void deveExcluirEstoque_quandoExistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.of(getEstoque(id, UUID.randomUUID(), UUID.randomUUID(), 1)));

        estoqueService.delete(id);

        verify(repositoryPort).deleteById(id);
    }
}
