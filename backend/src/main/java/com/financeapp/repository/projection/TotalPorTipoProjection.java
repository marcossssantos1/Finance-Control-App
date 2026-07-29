package com.financeapp.repository.projection;

import com.financeapp.domain.TipoCategoria;

import java.math.BigDecimal;

public interface TotalPorTipoProjection {
    TipoCategoria getTipo();
    BigDecimal getTotal();
}
