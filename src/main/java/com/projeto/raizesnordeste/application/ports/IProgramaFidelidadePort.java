package com.projeto.raizesnordeste.application.ports;

import com.projeto.raizesnordeste.domain.model.HistoricoPontos;
import com.projeto.raizesnordeste.domain.model.ProgramaFidelidade;
import com.projeto.raizesnordeste.domain.model.SolicitacaoResgatePontos;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

public interface IProgramaFidelidadePort {
    ProgramaFidelidade criarPrograma(UUID idUsuario);
    ProgramaFidelidade findByUsuario(UUID idUsuario);
    Page<HistoricoPontos> findHistorico(UUID idUsuario, Integer page, Integer linesPerPage, String direction, String orderBy);
    void acumularPorCompra(UUID idUsuario, BigDecimal valorCompra);
    ProgramaFidelidade resgatar(SolicitacaoResgatePontos solicitacao);
    void estornarResgate(UUID idUsuario, Integer pontos);
}
