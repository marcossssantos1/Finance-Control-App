package com.financeapp.service;

import com.financeapp.domain.Categoria;
import com.financeapp.domain.Conta;
import com.financeapp.domain.TipoConta;
import com.financeapp.domain.Transacao;
import com.financeapp.domain.TipoCategoria;
import com.financeapp.domain.Usuario;
import com.financeapp.dto.CriarTransacaoRequest;
import com.financeapp.dto.TransacaoResponse;
import com.financeapp.exception.RecursoNaoEncontradoException;
import com.financeapp.exception.TipoTransacaoInvalidoException;
import com.financeapp.repository.TransacaoRepository;
import com.financeapp.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaService contaService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private TransacaoService transacaoService;

    private Usuario usuarioLogado;
    private Conta conta;
    private Categoria categoriaDespesa;

    @BeforeEach
    void setup() {
        usuarioLogado = Usuario.builder().id(10L).nome("Marcos").email("marcos@ex.com").build();
        conta = Conta.builder().id(1L).usuario(usuarioLogado).nome("Nubank").tipo(TipoConta.CORRENTE)
                .saldoInicial(BigDecimal.ZERO).build();
        categoriaDespesa = Categoria.builder().id(2L).usuario(usuarioLogado).nome("Alimentação")
                .tipo(TipoCategoria.DESPESA).build();

        lenient().when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(usuarioLogado);
        lenient().when(authenticatedUserProvider.getUsuarioIdAutenticado()).thenReturn(10L);
    }

    @Test
    void deveCriarTransacaoValida() {
        var request = new CriarTransacaoRequest(
                "Mercado", new BigDecimal("150.00"), TipoCategoria.DESPESA, 1L, 2L, LocalDate.now()
        );

        when(contaService.buscarPorIdEValidarDono(1L)).thenReturn(conta);
        when(categoriaService.buscarPorIdEValidarUso(2L)).thenReturn(categoriaDespesa);
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        TransacaoResponse response = transacaoService.criar(request);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).save(captor.capture());

        assertThat(captor.getValue().getUsuario().getId()).isEqualTo(10L);
        assertThat(response.descricao()).isEqualTo("Mercado");
        assertThat(response.valor()).isEqualByComparingTo("150.00");
    }

    @Test
    void deveLancarErroQuandoTipoDivergeDaCategoria() {
        var request = new CriarTransacaoRequest(
                "Mercado", new BigDecimal("150.00"), TipoCategoria.RECEITA, 1L, 2L, LocalDate.now()
        );

        when(contaService.buscarPorIdEValidarDono(1L)).thenReturn(conta);
        when(categoriaService.buscarPorIdEValidarUso(2L)).thenReturn(categoriaDespesa); // categoria é DESPESA

        assertThrows(TipoTransacaoInvalidoException.class, () -> transacaoService.criar(request));
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void devePropagarErroQuandoContaNaoPertenceAoUsuario() {
        var request = new CriarTransacaoRequest(
                "Mercado", new BigDecimal("150.00"), TipoCategoria.DESPESA, 99L, 2L, LocalDate.now()
        );

        when(contaService.buscarPorIdEValidarDono(99L)).thenThrow(RecursoNaoEncontradoException.conta(99L));

        assertThrows(RecursoNaoEncontradoException.class, () -> transacaoService.criar(request));
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoExcluirTransacaoDeOutroUsuario() {
        when(transacaoRepository.findByIdAndUsuarioId(5L, 10L)).thenReturn(java.util.Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> transacaoService.excluir(5L));
    }
}
