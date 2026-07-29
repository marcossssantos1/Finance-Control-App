package com.financeapp.service;

import com.financeapp.domain.Categoria;
import com.financeapp.domain.Usuario;
import com.financeapp.dto.CategoriaResponse;
import com.financeapp.dto.CriarCategoriaRequest;
import com.financeapp.exception.RecursoNaoEncontradoException;
import com.financeapp.repository.CategoriaRepository;
import com.financeapp.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public CategoriaResponse criar(CriarCategoriaRequest request) {
        Usuario usuario = authenticatedUserProvider.getUsuarioAutenticado();

        Categoria categoria = Categoria.builder()
                .usuario(usuario)
                .nome(request.nome())
                .tipo(request.tipo())
                .cor(request.cor())
                .build();

        Categoria salva = categoriaRepository.save(categoria);
        return CategoriaResponse.fromEntity(salva);
    }

    public List<CategoriaResponse> listarDisponiveisParaUsuario() {
        Long usuarioId = authenticatedUserProvider.getUsuarioIdAutenticado();
        return categoriaRepository.findDisponiveisParaUsuario(usuarioId)
                .stream()
                .map(CategoriaResponse::fromEntity)
                .toList();
    }

    /**
     * Valida se uma categoria pode ser usada pelo usuário logado: ou é uma
     * categoria padrão do sistema (usuario == null), ou é uma categoria
     * customizada que pertence a ele. Usado pelo Ticket F (Transação) para
     * não permitir classificar uma transação com categoria de outro usuário.
     */
    public Categoria buscarPorIdEValidarUso(Long categoriaId) {
        Long usuarioId = authenticatedUserProvider.getUsuarioIdAutenticado();

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> RecursoNaoEncontradoException.categoria(categoriaId));

        boolean ehPadrao = categoria.getUsuario() == null;
        boolean ehDoUsuario = categoria.getUsuario() != null && categoria.getUsuario().getId().equals(usuarioId);

        if (!ehPadrao && !ehDoUsuario) {
            throw RecursoNaoEncontradoException.categoria(categoriaId);
        }

        return categoria;
    }
}
