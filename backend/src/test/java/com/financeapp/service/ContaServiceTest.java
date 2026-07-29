package com.financeapp.service;

import com.financeapp.domain.Conta;
import com.financeapp.domain.TipoConta;
import com.financeapp.domain.Usuario;
import com.financeapp.dto.ContaResponse;
import com.financeapp.dto.CriarContaRequest;
import com.financeapp.exception.RecursoNaoEncontradoException;
import com.financeapp.repository.ContaRepository;
import com.financeapp.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private ContaService contaService;

    private Usuario usuarioLogado;

    @BeforeEach
    void setup() {
        usuarioLogado = Usuario.builder().id(10L).nome("Marcos").email("marcos@ex.com").build();
        lenient().when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(usuarioLogado);
        lenient().when(authenticatedUserProvider.getUsuarioIdAutenticado()).thenReturn(10L);
    }

    @Test
    void deveCriarContaVinculadaAoUsuarioAutenticado() {
        CriarContaRequest request = new CriarContaRequest("Nubank", TipoConta.CORRENTE, new BigDecimal("100.00"));

        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
            Conta c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ContaResponse response = contaService.criar(request);

        ArgumentCaptor<Conta> captor = ArgumentCaptor.forClass(Conta.class);
        verify(contaRepository).save(captor.capture());

        assertThat(captor.getValue().getUsuario().getId()).isEqualTo(10L);
        assertThat(response.nome()).isEqualTo("Nubank");
        assertThat(response.tipo()).isEqualTo(TipoConta.CORRENTE);
    }

    @Test
    void deveUsarSaldoZeroQuandoNaoInformado() {
        CriarContaRequest request = new CriarContaRequest("Carteira", TipoConta.CARTEIRA, null);

        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContaResponse response = contaService.criar(request);

        assertThat(response.saldoInicial()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void deveListarApenasContasDoUsuarioLogado() {
        Conta conta = Conta.builder().id(1L).usuario(usuarioLogado).nome("Nubank").tipo(TipoConta.CORRENTE)
                .saldoInicial(BigDecimal.TEN).build();

        when(contaRepository.findAllByUsuarioIdOrderByNomeAsc(10L)).thenReturn(List.of(conta));

        List<ContaResponse> resultado = contaService.listarDoUsuarioLogado();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("Nubank");
    }

    @Test
    void deveLancarNaoEncontradoQuandoContaNaoPertenceAoUsuario() {
        when(contaRepository.findByIdAndUsuarioId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> contaService.buscarPorIdEValidarDono(99L));
    }
}
