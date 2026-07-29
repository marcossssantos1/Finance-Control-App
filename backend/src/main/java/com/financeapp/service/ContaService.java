package com.financeapp.service;

import com.financeapp.domain.Conta;
import com.financeapp.domain.Usuario;
import com.financeapp.dto.ContaResponse;
import com.financeapp.dto.CriarContaRequest;
import com.financeapp.exception.RecursoNaoEncontradoException;
import com.financeapp.repository.ContaRepository;
import com.financeapp.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public ContaResponse criar(CriarContaRequest request) {
        Usuario usuario = authenticatedUserProvider.getUsuarioAutenticado();

        Conta conta = Conta.builder()
                .usuario(usuario)
                .nome(request.nome())
                .tipo(request.tipo())
                .saldoInicial(request.saldoInicial() != null ? request.saldoInicial() : BigDecimal.ZERO)
                .build();

        Conta salva = contaRepository.save(conta);
        return ContaResponse.fromEntity(salva);
    }

    public List<ContaResponse> listarDoUsuarioLogado() {
        Long usuarioId = authenticatedUserProvider.getUsuarioIdAutenticado();
        return contaRepository.findAllByUsuarioIdOrderByNomeAsc(usuarioId)
                .stream()
                .map(ContaResponse::fromEntity)
                .toList();
    }

    /**
     * Busca uma conta garantindo que ela pertence ao usuário autenticado.
     * Se a conta existir mas for de outro usuário, retorna 404 (não 403) —
     * assim não vazamos para o cliente que o recurso existe.
     *
     * Reutilizado pelo Ticket F (Transação) para validar a conta informada.
     */
    public Conta buscarPorIdEValidarDono(Long contaId) {
        Long usuarioId = authenticatedUserProvider.getUsuarioIdAutenticado();
        return contaRepository.findByIdAndUsuarioId(contaId, usuarioId)
                .orElseThrow(() -> RecursoNaoEncontradoException.conta(contaId));
    }
}
