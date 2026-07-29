package com.financeapp.dto;

import com.financeapp.domain.TipoCategoria;
import com.financeapp.domain.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoCategoria tipo,
        Long contaId,
        String contaNome,
        Long categoriaId,
        String categoriaNome,
        LocalDate dataTransacao,
        LocalDateTime dataCriacao
) {
    public static TransacaoResponse fromEntity(Transacao transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getTipo(),
                transacao.getConta().getId(),
                transacao.getConta().getNome(),
                transacao.getCategoria().getId(),
                transacao.getCategoria().getNome(),
                transacao.getDataTransacao(),
                transacao.getDataCriacao()
        );
    }
}
