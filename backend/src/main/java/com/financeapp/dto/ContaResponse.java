package com.financeapp.dto;

import com.financeapp.domain.Conta;
import com.financeapp.domain.TipoConta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContaResponse(
        Long id,
        String nome,
        TipoConta tipo,
        BigDecimal saldoInicial,
        LocalDateTime dataCriacao
) {
    public static ContaResponse fromEntity(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getNome(),
                conta.getTipo(),
                conta.getSaldoInicial(),
                conta.getDataCriacao()
        );
    }
}
