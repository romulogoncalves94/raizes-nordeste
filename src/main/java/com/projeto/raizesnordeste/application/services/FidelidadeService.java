package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IHistoricoPontosRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadePort;
import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadeRepositoryPort;
import com.projeto.raizesnordeste.domain.enums.TipoHistoricoPontosEnum;
import com.projeto.raizesnordeste.domain.model.HistoricoPontos;
import com.projeto.raizesnordeste.domain.model.ProgramaFidelidade;
import com.projeto.raizesnordeste.domain.model.SolicitacaoResgatePontos;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

public class FidelidadeService implements IProgramaFidelidadePort {

    private final IProgramaFidelidadeRepositoryPort repositoryPort;
    private final IHistoricoPontosRepositoryPort historicoRepositoryPort;

    public FidelidadeService(IProgramaFidelidadeRepositoryPort repositoryPort, IHistoricoPontosRepositoryPort historicoRepositoryPort) {
        this.repositoryPort = repositoryPort;
        this.historicoRepositoryPort = historicoRepositoryPort;
    }

    @Override
    @Transactional
    public ProgramaFidelidade criarPrograma(UUID idUsuario) {
        return repositoryPort.findByUsuarioId(idUsuario)
                .orElseGet(() -> {
                    ProgramaFidelidade programa = new ProgramaFidelidade();
                    programa.setIdUsuario(idUsuario);
                    programa.setSaldoPontos(0);
                    return repositoryPort.save(programa);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramaFidelidade findByUsuario(UUID idUsuario) {
        return repositoryPort.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não participa do programa de fidelidade"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoricoPontos> findHistorico(UUID idUsuario, Integer page, Integer linesPerPage, String direction, String orderBy) {
        ProgramaFidelidade programa = findByUsuario(idUsuario);

        PageRequest pageRequest = PageRequest.of(
                page,
                linesPerPage,
                Sort.Direction.valueOf(direction),
                orderBy
        );

        return historicoRepositoryPort.findByProgramaFidelidadeId(programa.getId(), pageRequest);
    }

    @Override
    @Transactional
    public void acumularPorCompra(UUID idUsuario, BigDecimal valorCompra) {
        Optional<ProgramaFidelidade> programaOpt = repositoryPort.findByUsuarioId(idUsuario);

        if (programaOpt.isEmpty()) {
            return;
        }

        int pontos = valorCompra.setScale(0, RoundingMode.DOWN).intValue();

        if (pontos <= 0) {
            return;
        }

        ProgramaFidelidade programa = programaOpt.get();
        programa.setSaldoPontos(programa.getSaldoPontos() + pontos);
        repositoryPort.update(programa);

        HistoricoPontos historico = new HistoricoPontos();
        historico.setIdProgramaFidelidade(programa.getId());
        historico.setPontos(pontos);
        historico.setTipoHistorico(TipoHistoricoPontosEnum.ACUMULADO);
        historicoRepositoryPort.save(historico);
    }

    @Override
    @Transactional
    public ProgramaFidelidade resgatar(SolicitacaoResgatePontos solicitacao) {
        ProgramaFidelidade programa = findByUsuario(solicitacao.getIdUsuario());

        if (programa.getSaldoPontos() < solicitacao.getPontos()) {
            throw new BusinessRuleException(
                    "Saldo de pontos insuficiente. Disponível: " + programa.getSaldoPontos() + ", solicitado: " + solicitacao.getPontos()
            );
        }

        programa.setSaldoPontos(programa.getSaldoPontos() - solicitacao.getPontos());
        ProgramaFidelidade atualizado = repositoryPort.update(programa);

        HistoricoPontos historico = new HistoricoPontos();
        historico.setIdProgramaFidelidade(programa.getId());
        historico.setPontos(solicitacao.getPontos());
        historico.setTipoHistorico(TipoHistoricoPontosEnum.RESGATE);
        historicoRepositoryPort.save(historico);

        return atualizado;
    }

    @Override
    @Transactional
    public void estornarResgate(UUID idUsuario, Integer pontos) {
        Optional<ProgramaFidelidade> programaOpt = repositoryPort.findByUsuarioId(idUsuario);

        if (programaOpt.isEmpty() || pontos == null || pontos <= 0) {
            return;
        }

        ProgramaFidelidade programa = programaOpt.get();
        programa.setSaldoPontos(programa.getSaldoPontos() + pontos);
        repositoryPort.update(programa);

        // Estorno registrado como ACUMULADO: o enum do schema não distingue "estorno" de "acúmulo por compra"
        HistoricoPontos historico = new HistoricoPontos();
        historico.setIdProgramaFidelidade(programa.getId());
        historico.setPontos(pontos);
        historico.setTipoHistorico(TipoHistoricoPontosEnum.ACUMULADO);
        historicoRepositoryPort.save(historico);
    }
}
