package com.financeapp.dto;

import com.financeapp.domain.TipoConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CriarContaRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Tipo é obrigatório")
        TipoConta tipo,

        BigDecimal saldoInicial
) {
}
