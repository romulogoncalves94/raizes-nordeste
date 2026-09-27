package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IHistoricoPontosRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadePort;
import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadeRepositoryPort;
import com.projeto.raizesnordeste.domain.enums.TipoHistoricoPontosEnum;
import com.projeto.raizesnordeste.domain.model.HistoricoPontos;
import com.projeto.raizesnordeste.domain.model.ProgramaFidelidade;
import com.projeto.raizesnordeste.domain.model.SolicitacaoResgatePontos;
import com.projeto.raizesnordeste.domain.model.Usuario;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
public class FidelidadeService implements IProgramaFidelidadePort {

    private final IProgramaFidelidadeRepositoryPort repositoryPort;
    private final IHistoricoPontosRepositoryPort historicoRepositoryPort;

    public FidelidadeService(IProgramaFidelidadeRepositoryPort repositoryPort, IHistoricoPontosRepositoryPort historicoRepositoryPort) {
        this.repositoryPort = repositoryPort;
        this.historicoRepositoryPort = historicoRepositoryPort;
    }

    @Override
    @Transactional
    public ProgramaFidelidade criarPrograma(Usuario usuario) {
        log.debug("Garantindo programa de fidelidade para usuário {}", usuario.getId());

        return repositoryPort.findByUsuarioId(usuario.getId())
                .map(programa -> {
                    log.debug("Programa de fidelidade {} já existente para usuário {}", programa.getId(), usuario.getId());
                    return programa;
                })
                .orElseGet(() -> {
                    ProgramaFidelidade programa = new ProgramaFidelidade();
                    programa.setIdUsuario(usuario.getId());
                    programa.setNomeUsuario(usuario.getNome());
                    programa.setSaldoPontos(0);
                    ProgramaFidelidade criado = repositoryPort.save(programa);
                    log.info("Programa de fidelidade {} criado para usuário {}", criado.getId(), usuario.getId());
                    return criado;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramaFidelidade findByUsuario(UUID idUsuario) {
        log.debug("Buscando programa de fidelidade do usuário {}", idUsuario);

        ProgramaFidelidade programa = repositoryPort.findByUsuarioId(idUsuario)
                .orElseThrow(() -> {
                    log.info("Usuário {} não participa do programa de fidelidade", idUsuario);
                    return new ResourceNotFoundException("Usuário não participa do programa de fidelidade");
                });

        log.debug("Programa de fidelidade {} encontrado para usuário {} (saldo={})", programa.getId(), idUsuario, programa.getSaldoPontos());
        return programa;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoricoPontos> findHistorico(UUID idUsuario, Integer page, Integer linesPerPage, String direction, String orderBy) {
        log.debug("Listando histórico de pontos do usuário {}: page={} linesPerPage={} direction={} orderBy={}",
                idUsuario, page, linesPerPage, direction, orderBy);

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
        log.debug("Calculando acúmulo de pontos para usuário {} (valor compra R$ {})", idUsuario, valorCompra);

        Optional<ProgramaFidelidade> programaOpt = repositoryPort.findByUsuarioId(idUsuario);

        if (programaOpt.isEmpty()) {
            log.debug("Usuário {} não participa do programa de fidelidade; nenhum ponto acumulado", idUsuario);
            return;
        }

        int pontos = valorCompra.setScale(0, RoundingMode.DOWN).intValue();

        if (pontos <= 0) {
            log.debug("Valor de compra R$ {} não gera pontos para usuário {}", valorCompra, idUsuario);
            return;
        }

        ProgramaFidelidade programa = programaOpt.get();
        int saldoAnterior = programa.getSaldoPontos();
        programa.setSaldoPontos(saldoAnterior + pontos);
        repositoryPort.update(programa);

        HistoricoPontos historicoPontos = buildHistoricoPontos(programa, pontos, TipoHistoricoPontosEnum.ACUMULADO);
        historicoRepositoryPort.save(historicoPontos);
        log.info("Usuário {} acumulou {} pontos (saldo {} -> {})", idUsuario, pontos, saldoAnterior, programa.getSaldoPontos());
    }

    @Override
    @Transactional
    public ProgramaFidelidade resgatar(SolicitacaoResgatePontos solicitacao) {
        log.info("Solicitado resgate de {} pontos para usuário {}", solicitacao.getPontos(), solicitacao.getIdUsuario());

        ProgramaFidelidade programa = findByUsuario(solicitacao.getIdUsuario());

        if (programa.getSaldoPontos() < solicitacao.getPontos()) {
            log.warn("Saldo de pontos insuficiente para usuário {}: disponível={} solicitado={}",
                    solicitacao.getIdUsuario(), programa.getSaldoPontos(), solicitacao.getPontos());
            throw new BusinessRuleException(
                    String.format("Saldo de pontos insuficiente. Disponível: %d, solicitado: %d", programa.getSaldoPontos(), solicitacao.getPontos())
            );
        }

        int saldoAnterior = programa.getSaldoPontos();
        programa.setSaldoPontos(saldoAnterior - solicitacao.getPontos());
        ProgramaFidelidade atualizado = repositoryPort.update(programa);

        HistoricoPontos historicoPontos = buildHistoricoPontos(programa, solicitacao.getPontos(), TipoHistoricoPontosEnum.RESGATE);
        historicoRepositoryPort.save(historicoPontos);
        log.info("Usuário {} resgatou {} pontos (saldo {} -> {})",
                solicitacao.getIdUsuario(), solicitacao.getPontos(), saldoAnterior, atualizado.getSaldoPontos());

        return atualizado;
    }

    @Override
    @Transactional
    public void estornarResgate(UUID idUsuario, Integer pontos) {
        log.debug("Estornando resgate de {} pontos para usuário {}", pontos, idUsuario);

        Optional<ProgramaFidelidade> programaOpt = repositoryPort.findByUsuarioId(idUsuario);

        if (programaOpt.isEmpty() || isNull(pontos) || pontos <= 0) {
            log.debug("Nenhum estorno aplicado para usuário {} (semPrograma={} pontos={})", idUsuario, programaOpt.isEmpty(), pontos);
            return;
        }

        ProgramaFidelidade programa = programaOpt.get();
        int saldoAnterior = programa.getSaldoPontos();
        programa.setSaldoPontos(saldoAnterior + pontos);
        repositoryPort.update(programa);

        HistoricoPontos historicoPontos = buildHistoricoPontos(programa, pontos, TipoHistoricoPontosEnum.ACUMULADO);
        historicoRepositoryPort.save(historicoPontos);
        log.info("Estorno de {} pontos para usuário {} (saldo {} -> {})", pontos, idUsuario, saldoAnterior, programa.getSaldoPontos());
    }

    private HistoricoPontos buildHistoricoPontos(ProgramaFidelidade programaFidelidade, Integer pontos, TipoHistoricoPontosEnum tipo) {
        HistoricoPontos historico = new HistoricoPontos();
        historico.setIdProgramaFidelidade(programaFidelidade.getId());
        historico.setPontos(pontos);
        historico.setTipoHistorico(tipo);
        return historico;
    }
}
