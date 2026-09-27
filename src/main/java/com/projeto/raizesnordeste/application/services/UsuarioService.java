package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadePort;
import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.application.ports.IUsuarioRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Usuario;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class UsuarioService implements IUsuarioPort {

    private final IUsuarioRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final IProgramaFidelidadePort programaFidelidadePort;

    public UsuarioService(IUsuarioRepositoryPort repositoryPort, PasswordEncoder passwordEncoder, IProgramaFidelidadePort programaFidelidadePort) {
        this.repositoryPort = repositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.programaFidelidadePort = programaFidelidadePort;
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        log.info("Cadastrando novo usuário: email={}, perfil={}, cpf={}",
                usuario.getEmail(), usuario.getPerfil(), mascararCpf(usuario.getCpf()));

        validarCpfDuplicado(usuario.getCpf());
        validarEmailDuplicado(usuario.getEmail());

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        Usuario usuarioSalvo = repositoryPort.save(usuario);
        log.info("Usuário {} cadastrado com sucesso (perfil={})", usuarioSalvo.getId(), usuarioSalvo.getPerfil());

        if (Boolean.TRUE.equals(usuarioSalvo.getAceiteFidelidade())) {
            programaFidelidadePort.criarPrograma(usuarioSalvo);
            log.info("Programa de fidelidade criado para usuário {} (aceite registrado no cadastro)", usuarioSalvo.getId());
        }

        return usuarioSalvo;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findById(UUID id) {
        log.debug("Buscando usuário {}", id);

        Usuario usuario = repositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.info("Usuário {} não encontrado", id);
                    return new ResourceNotFoundException("Usuário não encontrado");
                });

        log.debug("Usuário {} encontrado (perfil={})", usuario.getId(), usuario.getPerfil());
        return usuario;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findByEmail(String email) {
        log.debug("Buscando usuário por email {}", email);

        Usuario usuario = repositoryPort.findByEmail(email)
                .orElseThrow(() -> {
                    log.info("Usuário com email {} não encontrado", email);
                    return new ResourceNotFoundException("Usuário não encontrado");
                });

        log.debug("Usuário {} encontrado pelo email {}", usuario.getId(), email);
        return usuario;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
        log.debug("Listando usuários: page={} linesPerPage={} direction={} orderBy={}", page, linesPerPage, direction, orderBy);

        PageRequest pageRequest = PageRequest.of(
                page,
                linesPerPage,
                Sort.Direction.valueOf(direction),
                orderBy
        );

        return repositoryPort.findAll(pageRequest);
    }

    @Override
    @Transactional
    public Usuario update(UUID id, Usuario usuario, boolean senhaAlterada) {
        log.info("Atualizando usuário {} (senhaAlterada={})", id, senhaAlterada);

        if (nonNull(usuario.getCpf()) && repositoryPort.existsByCpf(usuario.getCpf(), id)) {
            log.warn("Tentativa de atualizar usuário {} com CPF já cadastrado", id);
            throw new BusinessRuleException("CPF já cadastrado: " + usuario.getCpf());
        }

        if (nonNull(usuario.getEmail()) && repositoryPort.existsByEmail(usuario.getEmail(), id)) {
            log.warn("Tentativa de atualizar usuário {} com email já cadastrado: {}", id, usuario.getEmail());
            throw new BusinessRuleException("Email já cadastrado: " + usuario.getEmail());
        }

        if (senhaAlterada) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            log.info("Senha do usuário {} redefinida", id);
        }

        Usuario usuarioAtualizado = repositoryPort.update(usuario);
        log.info("Usuário {} atualizado com sucesso", id);

        if (Boolean.TRUE.equals(usuarioAtualizado.getAceiteFidelidade())) {
            programaFidelidadePort.criarPrograma(usuarioAtualizado);
            log.info("Programa de fidelidade garantido para usuário {} após atualização de aceite", id);
        }

        return usuarioAtualizado;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Solicitada remoção do usuário {}", id);

        findById(id);
        repositoryPort.deleteById(id);
        log.info("Usuário {} removido", id);
    }

    private void validarCpfDuplicado(String cpf) {
        if (repositoryPort.existsByCpf(cpf, null)) {
            log.warn("Tentativa de cadastro com CPF já existente");
            throw new BusinessRuleException("CPF já cadastrado: " + cpf);
        }
    }

    private void validarEmailDuplicado(String email) {
        if (repositoryPort.existsByEmail(email, null)) {
            log.warn("Tentativa de cadastro com email já existente: {}", email);
            throw new BusinessRuleException("Email já cadastrado: " + email);
        }
    }

    private String mascararCpf(String cpf) {
        if (isNull(cpf) || cpf.length() < 4) {
            return "***";
        }
        return "***." + cpf.substring(cpf.length() - 6);
    }
}
