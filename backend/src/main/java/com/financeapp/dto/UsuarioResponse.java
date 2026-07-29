package com.financeapp.dto;

import com.financeapp.domain.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        LocalDateTime dataCriacao
) {
    public static UsuarioResponse fromEntity(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDataCriacao()
        );
    }
}
