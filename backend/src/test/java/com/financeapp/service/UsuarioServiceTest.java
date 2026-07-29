package com.financeapp.service;

import com.financeapp.domain.Usuario;
import com.financeapp.dto.CriarUsuarioRequest;
import com.financeapp.dto.UsuarioResponse;
import com.financeapp.exception.EmailJaCadastradoException;
import com.financeapp.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    // Usamos a implementação real do BCrypt (não mockada) para validar
    // de verdade que a senha salva não é igual ao texto puro.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCriarUsuarioComSenhaCriptografada() {
        // arrange
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
        CriarUsuarioRequest request = new CriarUsuarioRequest("Marcos Santos", "marcos@example.com", "senhaForte123");

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            u.setDataCriacao(LocalDateTime.now());
            return u;
        });

        // act
        UsuarioResponse response = usuarioService.criar(request);

        // assert
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioPersistido = captor.getValue();

        assertThat(usuarioPersistido.getSenha()).isNotEqualTo("senhaForte123");
        assertThat(passwordEncoder.matches("senhaForte123", usuarioPersistido.getSenha())).isTrue();

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Marcos Santos");
        assertThat(response.email()).isEqualTo("marcos@example.com");
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        // arrange
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
        CriarUsuarioRequest request = new CriarUsuarioRequest("Marcos Santos", "marcos@example.com", "senhaForte123");

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        // act + assert
        assertThrows(EmailJaCadastradoException.class, () -> usuarioService.criar(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
