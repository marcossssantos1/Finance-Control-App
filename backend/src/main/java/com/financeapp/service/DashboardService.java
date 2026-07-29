package com.financeapp.service;

import com.financeapp.domain.TipoCategoria;
import com.financeapp.dto.DashboardResumoResponse;
import com.financeapp.dto.ResumoCategoriaResponse;
import com.financeapp.repository.TransacaoRepository;
import com.financeapp.repository.projection.TotalPorCategoriaProjection;
import com.financeapp.repository.projection.TotalPorTipoProjection;
import com.financeapp.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransacaoRepository transacaoRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DashboardResumoResponse obterResumo(LocalDate dataInicio, LocalDate dataFim) {
        Long usuarioId = authenticatedUserProvider.getUsuarioIdAutenticado();

        List<TotalPorTipoProjection> totaisPorTipo =
                transacaoRepository.somarPorTipo(usuarioId, dataInicio, dataFim);

        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;

        for (TotalPorTipoProjection projecao : totaisPorTipo) {
            if (projecao.getTipo() == TipoCategoria.RECEITA) {
                totalReceitas = projecao.getTotal();
            } else if (projecao.getTipo() == TipoCategoria.DESPESA) {
                totalDespesas = projecao.getTotal();
            }
        }

        List<ResumoCategoriaResponse> porCategoria = transacaoRepository
                .somarPorCategoria(usuarioId, dataInicio, dataFim)
                .stream()
                .map(this::toResumoCategoriaResponse)
                .toList();

        BigDecimal saldoPeriodo = totalReceitas.subtract(totalDespesas);

        return new DashboardResumoResponse(totalReceitas, totalDespesas, saldoPeriodo, porCategoria);
    }

    private ResumoCategoriaResponse toResumoCategoriaResponse(TotalPorCategoriaProjection p) {
        return new ResumoCategoriaResponse(p.getCategoriaId(), p.getCategoriaNome(), p.getTipo(), p.getTotal());
    }
}
