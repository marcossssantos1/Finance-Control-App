package com.financeapp.service;

import com.financeapp.domain.TipoCategoria;
import com.financeapp.dto.DashboardResumoResponse;
import com.financeapp.repository.TransacaoRepository;
import com.financeapp.repository.projection.TotalPorCategoriaProjection;
import com.financeapp.repository.projection.TotalPorTipoProjection;
import com.financeapp.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setup() {
        lenient().when(authenticatedUserProvider.getUsuarioIdAutenticado()).thenReturn(10L);
    }

    @Test
    void deveCalcularResumoComReceitasEDespesas() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 1, 31);

        TotalPorTipoProjection receita = totalPorTipo(TipoCategoria.RECEITA, new BigDecimal("1000.00"));
        TotalPorTipoProjection despesa = totalPorTipo(TipoCategoria.DESPESA, new BigDecimal("400.00"));

        when(transacaoRepository.somarPorTipo(10L, inicio, fim)).thenReturn(List.of(receita, despesa));

        TotalPorCategoriaProjection categoriaProjecao =
                totalPorCategoria(1L, "Salário", TipoCategoria.RECEITA, new BigDecimal("1000.00"));
        when(transacaoRepository.somarPorCategoria(10L, inicio, fim)).thenReturn(List.of(categoriaProjecao));

        DashboardResumoResponse resumo = dashboardService.obterResumo(inicio, fim);

        assertThat(resumo.totalReceitas()).isEqualByComparingTo("1000.00");
        assertThat(resumo.totalDespesas()).isEqualByComparingTo("400.00");
        assertThat(resumo.saldoPeriodo()).isEqualByComparingTo("600.00");
        assertThat(resumo.porCategoria()).hasSize(1);
        assertThat(resumo.porCategoria().get(0).categoriaNome()).isEqualTo("Salário");
    }

    @Test
    void deveRetornarZerosQuandoNaoHaTransacoesNoPeriodo() {
        when(transacaoRepository.somarPorTipo(10L, null, null)).thenReturn(List.of());
        when(transacaoRepository.somarPorCategoria(10L, null, null)).thenReturn(List.of());

        DashboardResumoResponse resumo = dashboardService.obterResumo(null, null);

        assertThat(resumo.totalReceitas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.totalDespesas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.saldoPeriodo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.porCategoria()).isEmpty();
    }

    private TotalPorTipoProjection totalPorTipo(TipoCategoria tipo, BigDecimal total) {
        return new TotalPorTipoProjection() {
            public TipoCategoria getTipo() { return tipo; }
            public BigDecimal getTotal() { return total; }
        };
    }

    private TotalPorCategoriaProjection totalPorCategoria(Long id, String nome, TipoCategoria tipo, BigDecimal total) {
        return new TotalPorCategoriaProjection() {
            public Long getCategoriaId() { return id; }
            public String getCategoriaNome() { return nome; }
            public TipoCategoria getTipo() { return tipo; }
            public BigDecimal getTotal() { return total; }
        };
    }
}
