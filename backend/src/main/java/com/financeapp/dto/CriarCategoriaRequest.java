package com.financeapp.dto;

import com.financeapp.domain.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarCategoriaRequest(
        String cor
) {
}
