package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IUnidadeRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Unidade;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeService")
class UnidadeServiceTest {

    @Mock
    private IUnidadeRepositoryPort repositoryPort;

    @InjectMocks
    private UnidadeService unidadeService;

    private Unidade getUnidade(UUID id, String cnpj) {
        return new Unidade(id, "Raízes Nordeste - Centro", cnpj, "60000-000", "Rua Principal", "Centro", "CE");
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao salvar unidade com CNPJ duplicado")
    void deveLancarBusinessRuleException_quandoSalvarUnidadeComCnpjDuplicado() {
        Unidade unidade = getUnidade(null, "12345678000100");
        when(repositoryPort.existsByCnpj("12345678000100", null)).thenReturn(true);

        assertThatThrownBy(() -> unidadeService.save(unidade))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("12345678000100");

        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar unidade quando CNPJ não é duplicado")
    void deveSalvarUnidade_quandoCnpjNaoDuplicado() {
        Unidade unidade = getUnidade(null, "12345678000100");
        when(repositoryPort.existsByCnpj("12345678000100", null)).thenReturn(false);
        when(repositoryPort.save(unidade)).thenReturn(getUnidade(UUID.randomUUID(), "12345678000100"));

        Unidade salva = unidadeService.save(unidade);

        assertThat(salva.getId()).isNotNull();
        verify(repositoryPort, times(1)).save(unidade);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando unidade não é encontrada por id")
    void deveLancarResourceNotFoundException_quandoUnidadeNaoEncontradaPorId() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unidadeService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve retornar unidade quando encontrada por id")
    void deveRetornarUnidade_quandoEncontradaPorId() {
        UUID id = UUID.randomUUID();
        Unidade unidade = getUnidade(id, "12345678000100");
        when(repositoryPort.findById(id)).thenReturn(Optional.of(unidade));

        Unidade encontrada = unidadeService.findById(id);

        assertThat(encontrada).isEqualTo(unidade);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao atualizar unidade com CNPJ duplicado de outro registro")
    void deveLancarBusinessRuleException_quandoAtualizarUnidadeComCnpjDuplicadoDeOutroRegistro() {
        UUID id = UUID.randomUUID();
        Unidade unidade = getUnidade(id, "99999999000199");
        when(repositoryPort.existsByCnpj("99999999000199", id)).thenReturn(true);

        assertThatThrownBy(() -> unidadeService.update(id, unidade))
                .isInstanceOf(BusinessRuleException.class);

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Deve atualizar unidade quando CNPJ não é duplicado")
    void deveAtualizarUnidade_quandoCnpjNaoDuplicado() {
        UUID id = UUID.randomUUID();
        Unidade unidade = getUnidade(id, "99999999000199");
        when(repositoryPort.existsByCnpj("99999999000199", id)).thenReturn(false);
        when(repositoryPort.update(unidade)).thenReturn(unidade);

        Unidade atualizada = unidadeService.update(id, unidade);

        assertThat(atualizada).isEqualTo(unidade);
    }

    @Test
    @DisplayName("Deve atualizar unidade quando CNPJ é nulo (sem checar duplicidade)")
    void deveAtualizarUnidade_quandoCnpjNulo() {
        UUID id = UUID.randomUUID();
        Unidade unidade = getUnidade(id, null);
        when(repositoryPort.update(unidade)).thenReturn(unidade);

        Unidade atualizada = unidadeService.update(id, unidade);

        assertThat(atualizada).isEqualTo(unidade);
        verify(repositoryPort, never()).existsByCnpj(any(), eq(id));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao excluir unidade inexistente")
    void deveLancarResourceNotFoundException_quandoExcluirUnidadeInexistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unidadeService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve excluir unidade quando existente")
    void deveExcluirUnidade_quandoExistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.of(getUnidade(id, "12345678000100")));

        unidadeService.delete(id);

        verify(repositoryPort).deleteById(id);
    }
}
