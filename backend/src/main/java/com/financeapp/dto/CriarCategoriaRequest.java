package com.financeapp.dto;

import com.financeapp.domain.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarCategoriaRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Tipo é obrigatório")
        TipoCategoria tipo,

        String cor
) {
}
