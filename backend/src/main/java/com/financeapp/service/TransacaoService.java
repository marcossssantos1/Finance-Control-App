package com.financeapp.service;

import com.financeapp.domain.Categoria;
import com.financeapp.domain.Conta;
import com.financeapp.domain.Transacao;
import com.financeapp.domain.TipoCategoria;
import com.financeapp.domain.Usuario;
import com.financeapp.dto.CriarTransacaoRequest;
import com.financeapp.dto.TransacaoResponse;
import com.financeapp.exception.RecursoNaoEncontradoException;
import com.financeapp.exception.TipoTransacaoInvalidoException;
import com.financeapp.repository.TransacaoRepository;
import com.financeapp.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaService contaService;
    private final CategoriaService categoriaService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public TransacaoResponse criar(CriarTransacaoRequest request) {
        Usuario usuario = authenticatedUserProvider.getUsuarioAutenticado();

        // Ambos os métodos abaixo já lançam RecursoNaoEncontradoException (404)
        // se o recurso não existir ou não pertencer/estiver disponível ao usuário logado.
        Conta conta = contaService.buscarPorIdEValidarDono(request.contaId());
        Categoria categoria = categoriaService.buscarPorIdEValidarUso(request.categoriaId());

        if (categoria.getTipo() != request.tipo()) {
            throw TipoTransacaoInvalidoException.tipoDivergenteDaCategoria();
        }

        Transacao transacao = Transacao.builder()
                .usuario(usuario)
                .conta(conta)
                .categoria(categoria)
                .descricao(request.descricao())
                .valor(request.valor())
                .tipo(request.tipo())
                .dataTransacao(request.dataTransacao())
                .build();

        Transacao salva = transacaoRepository.save(transacao);
        return TransacaoResponse.fromEntity(salva);
    }

    public Page<TransacaoResponse> listar(
            LocalDate dataInicio,
            LocalDate dataFim,
            Long contaId,
            Long categoriaId,
            Pageable pageable
    ) {
        Long usuarioId = authenticatedUserProvider.getUsuarioIdAutenticado();

        // Se contaId/categoriaId forem informados, valida que pertencem/estão
        // disponíveis ao usuário logado ANTES de usar como filtro — evita que
        // alguém descubra se existem transações filtrando por recurso alheio.
        if (contaId != null) {
            contaService.buscarPorIdEValidarDono(contaId);
        }
        if (categoriaId != null) {
            categoriaService.buscarPorIdEValidarUso(categoriaId);
        }

        return transacaoRepository
                .buscarComFiltros(usuarioId, dataInicio, dataFim, contaId, categoriaId, pageable)
                .map(TransacaoResponse::fromEntity);
    }

    @Transactional
    public void excluir(Long transacaoId) {
        Long usuarioId = authenticatedUserProvider.getUsuarioIdAutenticado();

        Transacao transacao = transacaoRepository.findByIdAndUsuarioId(transacaoId, usuarioId)
                .orElseThrow(() -> RecursoNaoEncontradoException.transacao(transacaoId));

        transacaoRepository.delete(transacao);
    }

    /**
     * Saldo atual da conta = saldo inicial + receitas - despesas.
     * Sem endpoint próprio ainda — será usado no dashboard (Onda 3).
     */
    public BigDecimal calcularSaldoConta(Long contaId) {
        Conta conta = contaService.buscarPorIdEValidarDono(contaId);
        List<Transacao> transacoes = transacaoRepository.findAllByContaId(contaId);

        BigDecimal saldo = conta.getSaldoInicial();
        for (Transacao t : transacoes) {
            saldo = t.getTipo() == TipoCategoria.RECEITA
                    ? saldo.add(t.getValor())
                    : saldo.subtract(t.getValor());
        }
        return saldo;
    }
}
