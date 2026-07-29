package com.financeapp.service;

import com.financeapp.domain.Usuario;
import com.financeapp.dto.CriarUsuarioRequest;
import com.financeapp.dto.UsuarioResponse;
import com.financeapp.exception.EmailJaCadastradoException;
import com.financeapp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(salvo);
    }
}
