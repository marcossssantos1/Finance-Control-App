package com.financeapp.service;

import com.financeapp.domain.Usuario;
import com.financeapp.dto.CriarUsuarioRequest;
import com.financeapp.dto.UsuarioResponse;
import com.financeapp.exception.EmailJaCadastradoException;
import com.financeapp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografada() {
        CriarUsuarioRequest request = new CriarUsuarioRequest("Ana Silva", "ana@exemplo.com", "senhaSegura123");
        LocalDateTime dataCriacao = LocalDateTime.now();
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            usuario.setDataCriacao(dataCriacao);
            return usuario;
        });

        UsuarioResponse response = usuarioService.criar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioSalvo = captor.getValue();
        assertThat(usuarioSalvo.getSenha()).isNotEqualTo(request.senha());
        assertThat(passwordEncoder.matches(request.senha(), usuarioSalvo.getSenha())).isTrue();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Ana Silva");
        assertThat(response.email()).isEqualTo("ana@exemplo.com");
        assertThat(response.dataCriacao()).isEqualTo(dataCriacao);
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        CriarUsuarioRequest request = new CriarUsuarioRequest("Ana Silva", "ana@exemplo.com", "senhaSegura123");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request))
                .isInstanceOf(EmailJaCadastradoException.class)
                .hasMessageContaining(request.email());

        verify(usuarioRepository, never()).save(any());
    }
}
