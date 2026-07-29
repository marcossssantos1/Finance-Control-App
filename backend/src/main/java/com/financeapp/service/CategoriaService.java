package com.financeapp.service;

import com.financeapp.domain.Categoria;
import com.financeapp.domain.Usuario;
import com.financeapp.dto.CategoriaResponse;
import com.financeapp.dto.CriarCategoriaRequest;
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

        return CategoriaResponse.fromEntity(categoriaRepository.save(categoria));
    }

    public List<CategoriaResponse> listarDisponiveisParaUsuario() {
        Long usuarioId = authenticatedUserProvider.getUsuarioIdAutenticado();
        return categoriaRepository.findDisponiveisParaUsuario(usuarioId)
                .stream()
                .map(CategoriaResponse::fromEntity)
                .toList();
    }
}
