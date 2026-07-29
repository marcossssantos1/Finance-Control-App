package com.financeapp.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResumoResponse(
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldoPeriodo,
        List<ResumoCategoriaResponse> porCategoria
) {
}
