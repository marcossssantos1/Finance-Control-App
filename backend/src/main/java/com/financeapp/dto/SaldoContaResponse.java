package com.financeapp.dto;

import java.math.BigDecimal;

public record SaldoContaResponse(
        Long contaId,
        String contaNome,
        BigDecimal saldoAtual
) {
}
