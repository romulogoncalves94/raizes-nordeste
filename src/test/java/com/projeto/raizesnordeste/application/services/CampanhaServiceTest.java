package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.ICampanhaRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Campanha;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CampanhaService")
class CampanhaServiceTest {

    @Mock
    private ICampanhaRepositoryPort repositoryPort;

    @InjectMocks
    private CampanhaService campanhaService;

    private Campanha getCampanha(UUID id, String nome, BigDecimal percentual, LocalDateTime inicio, LocalDateTime fim, Boolean ativa) {
        return new Campanha(id, nome, percentual, inicio, fim, ativa);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando dataFim é igual à dataInicio")
    void deveLancarBusinessRuleException_quandoDataFimIgualDataInicio() {
        LocalDateTime data = LocalDateTime.now();
        Campanha campanha = getCampanha(null, "Semana Nordestina", BigDecimal.TEN, data, data, true);

        assertThatThrownBy(() -> campanhaService.save(campanha))
                .isInstanceOf(BusinessRuleException.class);

        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando dataFim é anterior à dataInicio")
    void deveLancarBusinessRuleException_quandoDataFimAnteriorADataInicio() {
        LocalDateTime data = LocalDateTime.now();
        Campanha campanha = getCampanha(null, "Semana Nordestina", BigDecimal.TEN, data, data.minusDays(1), true);

        assertThatThrownBy(() -> campanhaService.save(campanha))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Deve salvar sem validar vigência quando datas são válidas")
    void deveSalvarSemValidarVigencia_quandoDatasSaoValidas() {
        LocalDateTime data = LocalDateTime.now();
        Campanha campanha = getCampanha(null, "Sem prazo definido", BigDecimal.TEN, data, data.plusDays(7), true);
        when(repositoryPort.save(campanha)).thenReturn(campanha);

        Campanha resultado = campanhaService.save(campanha);

        assertThat(resultado).isEqualTo(campanha);
    }

    @Test
    @DisplayName("Deve definir ativa como true quando ativa não é informada")
    void deveDefinirAtivaComoTrue_quandoAtivaNaoInformada() {
        LocalDateTime inicio = LocalDateTime.now();
        Campanha campanha = getCampanha(null, "Semana Nordestina", BigDecimal.TEN, inicio, inicio.plusDays(7), null);
        when(repositoryPort.save(campanha)).thenReturn(campanha);

        Campanha resultado = campanhaService.save(campanha);

        assertThat(resultado.getAtiva()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando campanha não é encontrada por id")
    void deveLancarResourceNotFoundException_quandoCampanhaNaoEncontradaPorId() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campanhaService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve delegar ao repositório com data atual ao buscar campanhas vigentes")
    void deveDelegarAoRepositorioComDataAtual_quandoBuscarVigentes() {
        when(repositoryPort.findCampanhasVigentes(any(LocalDateTime.class))).thenReturn(List.of());

        List<Campanha> vigentes = campanhaService.findCampanhasVigentes();

        assertThat(vigentes).isEmpty();
        verify(repositoryPort).findCampanhasVigentes(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando não há campanha vigente")
    void deveRetornarOptionalVazio_quandoNaoHaCampanhaVigente() {
        when(repositoryPort.findCampanhasVigentes(any(LocalDateTime.class))).thenReturn(List.of());

        Optional<Campanha> resultado = campanhaService.findMelhorVigente();

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve escolher campanha com maior percentual de desconto quando múltiplas vigentes")
    void deveEscolherCampanhaComMaiorPercentualDesconto_quandoMultiplasVigentes() {
        Campanha menor = getCampanha(UUID.randomUUID(), "Desconto 10", new BigDecimal("10"), null, null, true);
        Campanha maior = getCampanha(UUID.randomUUID(), "Desconto 25", new BigDecimal("25"), null, null, true);
        when(repositoryPort.findCampanhasVigentes(any(LocalDateTime.class))).thenReturn(List.of(menor, maior));

        Optional<Campanha> resultado = campanhaService.findMelhorVigente();

        assertThat(resultado).contains(maior);
    }

    @Test
    @DisplayName("Deve atualizar campanha quando vigência é válida")
    void deveAtualizarCampanha_quandoVigenciaValida() {
        UUID id = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.now();
        Campanha campanha = getCampanha(id, "Semana Nordestina", BigDecimal.TEN, inicio, inicio.plusDays(7), true);
        when(repositoryPort.update(campanha)).thenReturn(campanha);

        Campanha resultado = campanhaService.update(id, campanha);

        assertThat(resultado).isEqualTo(campanha);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao atualizar com vigência inválida")
    void deveLancarBusinessRuleException_quandoAtualizarComVigenciaInvalida() {
        UUID id = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.now();
        Campanha campanha = getCampanha(id, "Semana Nordestina", BigDecimal.TEN, inicio, inicio.minusHours(1), true);

        assertThatThrownBy(() -> campanhaService.update(id, campanha))
                .isInstanceOf(BusinessRuleException.class);

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao excluir campanha inexistente")
    void deveLancarResourceNotFoundException_quandoExcluirCampanhaInexistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campanhaService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve excluir campanha quando existente")
    void deveExcluirCampanha_quandoExistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.of(getCampanha(id, "Campanha", BigDecimal.TEN, null, null, true)));

        campanhaService.delete(id);

        verify(repositoryPort).deleteById(id);
    }
}
