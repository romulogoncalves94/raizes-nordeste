package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadePort;
import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.application.ports.IUsuarioRepositoryPort;
import com.projeto.raizesnordeste.domain.model.Usuario;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.nonNull;

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
        validarCpfDuplicado(usuario.getCpf());
        validarEmailDuplicado(usuario.getEmail());

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        Usuario usuarioSalvo = repositoryPort.save(usuario);

        if (Boolean.TRUE.equals(usuarioSalvo.getAceiteFidelidade())) {
            programaFidelidadePort.criarPrograma(usuarioSalvo.getId());
        }

        return usuarioSalvo;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findByEmail(String email) {
        return repositoryPort.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> findAll(Integer page, Integer linesPerPage, String direction, String orderBy) {
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

        if (nonNull(usuario.getCpf()) && repositoryPort.existsByCpf(usuario.getCpf(), id)) {
            throw new BusinessRuleException("CPF já cadastrado: " + usuario.getCpf());
        }

        if (nonNull(usuario.getEmail()) && repositoryPort.existsByEmail(usuario.getEmail(), id)) {
            throw new BusinessRuleException("Email já cadastrado: " + usuario.getEmail());
        }

        if (senhaAlterada) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

//        if (Boolean.FALSE.equals(usuarioExistente.getAceiteLgpd())) {
//            return repositoryPort.update(aplicarMascaraLgpd(usuarioExistente));
//        }

        Usuario usuarioAtualizado = repositoryPort.update(usuario);

        if (Boolean.TRUE.equals(usuarioAtualizado.getAceiteFidelidade())) {
            programaFidelidadePort.criarPrograma(usuarioAtualizado.getId());
        }

        return usuarioAtualizado;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findById(id);
        repositoryPort.deleteById(id);
    }

    private void validarCpfDuplicado(String cpf) {
        if (repositoryPort.existsByCpf(cpf, null)) {
            throw new BusinessRuleException("CPF já cadastrado: " + cpf);
        }
    }

    private void validarEmailDuplicado(String email) {
        if (repositoryPort.existsByEmail(email, null)) {
            throw new BusinessRuleException("Email já cadastrado: " + email);
        }
    }

//    private Usuario aplicarMascaraLgpd(Usuario usuario) {
//        return new Usuario(
//                usuario.getId(),
//                usuario.getNome(),
//                mascararCpfFormatado(usuario.getCpf()),
//                mascararEmail(usuario.getEmail()),
//                usuario.getSenha(),
//                usuario.getPerfil(),
//                usuario.getAceiteFidelidade(),
//                usuario.getAceiteLgpd()
//        );
//    }
//
//    private String mascararCpfFormatado(String cpf) {
//        if (cpf == null || cpf.length() < 11) {
//            return cpf;
//        }
//
//        return cpf.substring(0, 4) + "***.***" + cpf.substring(11);
//    }
//
//    private String mascararEmail(String email) {
//        if (email == null || !email.contains("@")) {
//            return email;
//        }
//
//        String[] partes = email.split("@", 2);
//        String usuario = partes[0];
//        String dominio = partes[1];
//
//        if (usuario.length() <= 2) {
//            return "*@" + dominio;
//        }
//
//        return usuario.charAt(0) + "***" + usuario.charAt(usuario.length() - 1) + "@" + dominio;
//    }
}
