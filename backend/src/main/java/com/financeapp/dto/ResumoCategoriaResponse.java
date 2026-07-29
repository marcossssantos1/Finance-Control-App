package com.financeapp.dto;

import com.financeapp.domain.TipoCategoria;

import java.math.BigDecimal;

public record ResumoCategoriaResponse(
        Long categoriaId,
        String categoriaNome,
        TipoCategoria tipo,
        BigDecimal total
) {
}
