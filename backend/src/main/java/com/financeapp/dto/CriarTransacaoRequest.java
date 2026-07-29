package com.financeapp.dto;

import com.financeapp.domain.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarTransacaoRequest(

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "Tipo é obrigatório")
        TipoCategoria tipo,

        @NotNull(message = "Conta é obrigatória")
        Long contaId,

        @NotNull(message = "Categoria é obrigatória")
        Long categoriaId,

        @NotNull(message = "Data da transação é obrigatória")
        LocalDate dataTransacao
) {
}
