package com.financeapp.dto;

import com.financeapp.domain.Categoria;
import com.financeapp.domain.TipoCategoria;

import java.time.LocalDateTime;

public record CategoriaResponse(
        Long id,
        String nome,
        TipoCategoria tipo,
        String cor,
        boolean padrao,
        LocalDateTime dataCriacao
) {
    public static CategoriaResponse fromEntity(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getTipo(),
                categoria.getCor(),
                categoria.getUsuario() == null,
                categoria.getDataCriacao()
        );
    }
}
