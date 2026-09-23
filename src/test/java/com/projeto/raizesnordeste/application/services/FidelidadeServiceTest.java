package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IHistoricoPontosRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadeRepositoryPort;
import com.projeto.raizesnordeste.domain.enums.TipoHistoricoPontosEnum;
import com.projeto.raizesnordeste.domain.model.HistoricoPontos;
import com.projeto.raizesnordeste.domain.model.ProgramaFidelidade;
import com.projeto.raizesnordeste.domain.model.SolicitacaoResgatePontos;
import com.projeto.raizesnordeste.domain.model.Usuario;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("FidelidadeService")
class FidelidadeServiceTest {

    @Mock
    private IProgramaFidelidadeRepositoryPort repositoryPort;

    @Mock
    private IHistoricoPontosRepositoryPort historicoRepositoryPort;

    @InjectMocks
    private FidelidadeService fidelidadeService;

    private ProgramaFidelidade getPrograma(UUID id, UUID idUsuario, Integer saldoPontos) {
        return new ProgramaFidelidade(id, idUsuario, "Maria Nordeste", saldoPontos);
    }

    private Usuario getUsuario() {
        return new Usuario(UUID.randomUUID(), "Maria Nordeste");
    }

    @Test
    @DisplayName("Deve retornar programa existente sem salvar novamente quando criarPrograma já existente")
    void deveRetornarProgramaExistente_semSalvarNovamente_quandoCriarProgramaJaExistente() {
        Usuario usuario = getUsuario();
        ProgramaFidelidade existente = getPrograma(UUID.randomUUID(), usuario.getId(), 100);

        when(repositoryPort.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(existente));

        ProgramaFidelidade resultado = fidelidadeService.criarPrograma(usuario);

        assertThat(resultado).isEqualTo(existente);
        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar programa com saldo zero quando não existente")
    void deveCriarProgramaComSaldoZero_quandoNaoExistente() {
        Usuario usuario = getUsuario();

        when(repositoryPort.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());
        when(repositoryPort.save(any(ProgramaFidelidade.class))).thenAnswer(invocation -> {
            ProgramaFidelidade p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProgramaFidelidade resultado = fidelidadeService.criarPrograma(usuario);

        assertThat(resultado.getSaldoPontos()).isZero();
        assertThat(resultado.getIdUsuario()).isEqualTo(usuario.getId());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não participa do programa")
    void deveLancarResourceNotFoundException_quandoUsuarioNaoParticipaDoPrograma() {
        UUID idUsuario = UUID.randomUUID();
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fidelidadeService.findByUsuario(idUsuario))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Não deve acumular pontos quando usuário não participa do programa")
    void naoDeveAcumularPontos_quandoUsuarioNaoParticipaDoPrograma() {
        UUID idUsuario = UUID.randomUUID();
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.empty());

        fidelidadeService.acumularPorCompra(idUsuario, new BigDecimal("50.00"));

        verify(repositoryPort, never()).update(any());
        verify(historicoRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Não deve acumular pontos quando valor arredondado para baixo resulta em zero")
    void naoDeveAcumularPontos_quandoValorArredondadoParaBaixoResultaEmZero() {
        UUID idUsuario = UUID.randomUUID();
        ProgramaFidelidade programa = getPrograma(UUID.randomUUID(), idUsuario, 10);
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.of(programa));

        fidelidadeService.acumularPorCompra(idUsuario, new BigDecimal("0.99"));

        verify(repositoryPort, never()).update(any());
        verify(historicoRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve acumular pontos truncados e registrar histórico ACUMULADO")
    void deveAcumularPontosTruncados_eRegistrarHistoricoAcumulado() {
        UUID idUsuario = UUID.randomUUID();
        ProgramaFidelidade programa = getPrograma(UUID.randomUUID(), idUsuario, 10);
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.of(programa));
        when(repositoryPort.update(programa)).thenReturn(programa);

        fidelidadeService.acumularPorCompra(idUsuario, new BigDecimal("9.99"));

        assertThat(programa.getSaldoPontos()).isEqualTo(19);

        ArgumentCaptor<HistoricoPontos> captor = ArgumentCaptor.forClass(HistoricoPontos.class);
        verify(historicoRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getPontos()).isEqualTo(9);
        assertThat(captor.getValue().getTipoHistorico()).isEqualTo(TipoHistoricoPontosEnum.ACUMULADO);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao resgatar com saldo insuficiente")
    void deveLancarBusinessRuleException_quandoResgatarComSaldoInsuficiente() {
        UUID idUsuario = UUID.randomUUID();
        ProgramaFidelidade programa = getPrograma(UUID.randomUUID(), idUsuario, 50);
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.of(programa));

        assertThatThrownBy(() -> fidelidadeService.resgatar(new SolicitacaoResgatePontos(idUsuario, 100)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Disponível: 50")
                .hasMessageContaining("solicitado: 100");

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Deve debitar saldo e registrar histórico RESGATE quando saldo suficiente")
    void deveDebitarSaldoERegistrarHistoricoResgate_quandoSaldoSuficiente() {
        UUID idUsuario = UUID.randomUUID();
        ProgramaFidelidade programa = getPrograma(UUID.randomUUID(), idUsuario, 200);
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.of(programa));
        when(repositoryPort.update(programa)).thenReturn(programa);

        ProgramaFidelidade resultado = fidelidadeService.resgatar(new SolicitacaoResgatePontos(idUsuario, 150));

        assertThat(resultado.getSaldoPontos()).isEqualTo(50);

        ArgumentCaptor<HistoricoPontos> captor = ArgumentCaptor.forClass(HistoricoPontos.class);
        verify(historicoRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getPontos()).isEqualTo(150);
        assertThat(captor.getValue().getTipoHistorico()).isEqualTo(TipoHistoricoPontosEnum.RESGATE);
    }

    @Test
    @DisplayName("Não deve estornar quando usuário não participa do programa")
    void naoDeveEstornar_quandoUsuarioNaoParticipaDoPrograma() {
        UUID idUsuario = UUID.randomUUID();
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.empty());

        fidelidadeService.estornarResgate(idUsuario, 100);

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Não deve estornar quando pontos são nulos ou não positivos")
    void naoDeveEstornar_quandoPontosNulosOuNaoPositivos() {
        UUID idUsuario = UUID.randomUUID();
        ProgramaFidelidade programa = getPrograma(UUID.randomUUID(), idUsuario, 10);
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.of(programa));

        fidelidadeService.estornarResgate(idUsuario, 0);
        fidelidadeService.estornarResgate(idUsuario, null);

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Deve creditar saldo de volta quando estorno de resgate válido")
    void deveCreditarSaldoDeVolta_quandoEstornarResgateValido() {
        UUID idUsuario = UUID.randomUUID();
        ProgramaFidelidade programa = getPrograma(UUID.randomUUID(), idUsuario, 10);
        when(repositoryPort.findByUsuarioId(idUsuario)).thenReturn(Optional.of(programa));
        when(repositoryPort.update(programa)).thenReturn(programa);

        fidelidadeService.estornarResgate(idUsuario, 30);

        assertThat(programa.getSaldoPontos()).isEqualTo(40);
        verify(historicoRepositoryPort).save(any(HistoricoPontos.class));
    }
}
