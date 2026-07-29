package com.financeapp.repository.projection;

import com.financeapp.domain.TipoCategoria;

import java.math.BigDecimal;

public interface TotalPorCategoriaProjection {
    Long getCategoriaId();
    String getCategoriaNome();
    TipoCategoria getTipo();
    BigDecimal getTotal();
}
