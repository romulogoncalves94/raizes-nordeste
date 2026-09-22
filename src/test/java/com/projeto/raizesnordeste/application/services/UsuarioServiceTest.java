package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadePort;
import com.projeto.raizesnordeste.application.ports.IUsuarioRepositoryPort;
import com.projeto.raizesnordeste.domain.enums.PerfilUsuarioEnum;
import com.projeto.raizesnordeste.domain.model.Usuario;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService")
class UsuarioServiceTest {

    @Mock
    private IUsuarioRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IProgramaFidelidadePort programaFidelidadePort;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario umUsuario(UUID id, String cpf, String email, Boolean aceiteFidelidade) {
        return new Usuario(id, "Maria Nordeste", cpf, email, "senha123", PerfilUsuarioEnum.CLIENTE, true, aceiteFidelidade);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao salvar usuário com CPF duplicado")
    void deveLancarBusinessRuleException_quandoSalvarUsuarioComCpfDuplicado() {
        Usuario usuario = umUsuario(null, "11111111111", "maria@raizes.com", false);
        when(repositoryPort.existsByCpf("11111111111", null)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.save(usuario))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("11111111111");

        verify(repositoryPort, never()).existsByEmail(any(), any());
        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao salvar usuário com email duplicado")
    void deveLancarBusinessRuleException_quandoSalvarUsuarioComEmailDuplicado() {
        Usuario usuario = umUsuario(null, "11111111111", "maria@raizes.com", false);
        when(repositoryPort.existsByCpf("11111111111", null)).thenReturn(false);
        when(repositoryPort.existsByEmail("maria@raizes.com", null)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.save(usuario))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("maria@raizes.com");

        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve codificar senha e salvar sem criar programa de fidelidade quando aceiteFidelidade é false")
    void deveCodificarSenhaESalvar_semCriarProgramaFidelidade_quandoAceiteFidelidadeFalse() {
        Usuario usuario = umUsuario(null, "11111111111", "maria@raizes.com", false);
        when(repositoryPort.existsByCpf("11111111111", null)).thenReturn(false);
        when(repositoryPort.existsByEmail("maria@raizes.com", null)).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senha-codificada");
        Usuario salvo = umUsuario(UUID.randomUUID(), "11111111111", "maria@raizes.com", false);
        when(repositoryPort.save(usuario)).thenReturn(salvo);

        Usuario resultado = usuarioService.save(usuario);

        assertThat(usuario.getSenha()).isEqualTo("senha-codificada");
        assertThat(resultado.getId()).isNotNull();
        verify(programaFidelidadePort, never()).criarPrograma(any());
    }

    @Test
    @DisplayName("Deve criar programa de fidelidade quando aceiteFidelidade é true")
    void deveCriarProgramaFidelidade_quandoAceiteFidelidadeTrue() {
        Usuario usuario = umUsuario(null, "11111111111", "maria@raizes.com", true);
        UUID idSalvo = UUID.randomUUID();
        when(repositoryPort.existsByCpf("11111111111", null)).thenReturn(false);
        when(repositoryPort.existsByEmail("maria@raizes.com", null)).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senha-codificada");
        when(repositoryPort.save(usuario)).thenReturn(umUsuario(idSalvo, "11111111111", "maria@raizes.com", true));

        usuarioService.save(usuario);

        verify(programaFidelidadePort).criarPrograma(idSalvo);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não é encontrado por id")
    void deveLancarResourceNotFoundException_quandoUsuarioNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não é encontrado por email")
    void deveLancarResourceNotFoundException_quandoUsuarioNaoEncontradoPorEmail() {
        when(repositoryPort.findByEmail("inexistente@raizes.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findByEmail("inexistente@raizes.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao atualizar com CPF duplicado de outro usuário")
    void deveLancarBusinessRuleException_quandoAtualizarComCpfDuplicadoDeOutroUsuario() {
        UUID id = UUID.randomUUID();
        Usuario usuario = umUsuario(id, "22222222222", "novo@raizes.com", false);
        when(repositoryPort.existsByCpf("22222222222", id)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.update(id, usuario, false))
                .isInstanceOf(BusinessRuleException.class);

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao atualizar com email duplicado de outro usuário")
    void deveLancarBusinessRuleException_quandoAtualizarComEmailDuplicadoDeOutroUsuario() {
        UUID id = UUID.randomUUID();
        Usuario usuario = umUsuario(id, "22222222222", "novo@raizes.com", false);
        when(repositoryPort.existsByCpf("22222222222", id)).thenReturn(false);
        when(repositoryPort.existsByEmail("novo@raizes.com", id)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.update(id, usuario, false))
                .isInstanceOf(BusinessRuleException.class);

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Deve recodificar senha quando senhaAlterada é true")
    void deveRecodificarSenha_quandoSenhaAlteradaTrue() {
        UUID id = UUID.randomUUID();
        Usuario usuario = umUsuario(id, "22222222222", "novo@raizes.com", false);
        usuario.setSenha("novaSenha");
        when(repositoryPort.existsByCpf("22222222222", id)).thenReturn(false);
        when(repositoryPort.existsByEmail("novo@raizes.com", id)).thenReturn(false);
        when(passwordEncoder.encode("novaSenha")).thenReturn("nova-senha-codificada");
        when(repositoryPort.update(usuario)).thenReturn(usuario);

        usuarioService.update(id, usuario, true);

        assertThat(usuario.getSenha()).isEqualTo("nova-senha-codificada");
        verify(passwordEncoder).encode("novaSenha");
    }

    @Test
    @DisplayName("Não deve recodificar senha quando senhaAlterada é false")
    void naoDeveRecodificarSenha_quandoSenhaAlteradaFalse() {
        UUID id = UUID.randomUUID();
        Usuario usuario = umUsuario(id, "22222222222", "novo@raizes.com", false);
        usuario.setSenha("hash-existente");
        when(repositoryPort.existsByCpf("22222222222", id)).thenReturn(false);
        when(repositoryPort.existsByEmail("novo@raizes.com", id)).thenReturn(false);
        when(repositoryPort.update(usuario)).thenReturn(usuario);

        usuarioService.update(id, usuario, false);

        assertThat(usuario.getSenha()).isEqualTo("hash-existente");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Deve criar programa de fidelidade ao atualizar usuário com aceiteFidelidade true")
    void deveCriarProgramaFidelidade_quandoAtualizarComAceiteFidelidadeTrue() {
        UUID id = UUID.randomUUID();
        Usuario usuario = umUsuario(id, "22222222222", "novo@raizes.com", true);
        when(repositoryPort.existsByCpf("22222222222", id)).thenReturn(false);
        when(repositoryPort.existsByEmail("novo@raizes.com", id)).thenReturn(false);
        when(repositoryPort.update(usuario)).thenReturn(usuario);

        usuarioService.update(id, usuario, false);

        verify(programaFidelidadePort).criarPrograma(id);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao excluir usuário inexistente")
    void deveLancarResourceNotFoundException_quandoExcluirUsuarioInexistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve excluir usuário quando existente")
    void deveExcluirUsuario_quandoExistente() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.of(umUsuario(id, "11111111111", "maria@raizes.com", false)));

        usuarioService.delete(id);

        verify(repositoryPort).deleteById(id);
    }
}
