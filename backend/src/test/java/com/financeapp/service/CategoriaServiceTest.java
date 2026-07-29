package com.financeapp.service;

import com.financeapp.domain.Categoria;
import com.financeapp.domain.TipoCategoria;
import com.financeapp.domain.Usuario;
import com.financeapp.dto.CategoriaResponse;
import com.financeapp.dto.CriarCategoriaRequest;
import com.financeapp.repository.CategoriaRepository;
import com.financeapp.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private CategoriaService categoriaService;

    private Usuario usuarioLogado;

    @BeforeEach
    void setup() {
        usuarioLogado = Usuario.builder().id(10L).nome("Marcos").email("marcos@exemplo.com").build();
        lenient().when(authenticatedUserProvider.getUsuarioAutenticado()).thenReturn(usuarioLogado);
        lenient().when(authenticatedUserProvider.getUsuarioIdAutenticado()).thenReturn(10L);
    }

    @Test
    void deveCriarCategoriaVinculadaAoUsuarioAutenticado() {
        CriarCategoriaRequest request = new CriarCategoriaRequest("Mercado", TipoCategoria.DESPESA, "#FF5733");
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(1L);
            return categoria;
        });

        CategoriaResponse response = categoriaService.criar(request);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isEqualTo(usuarioLogado);
        assertThat(response.nome()).isEqualTo("Mercado");
        assertThat(response.padrao()).isFalse();
    }

    @Test
    void deveListarCategoriasPadraoECustomizadasDoUsuario() {
        Categoria padrao = Categoria.builder().id(1L).nome("Alimentação").tipo(TipoCategoria.DESPESA).build();
        Categoria customizada = Categoria.builder().id(2L).usuario(usuarioLogado).nome("Pets").tipo(TipoCategoria.DESPESA).build();
        when(categoriaRepository.findDisponiveisParaUsuario(10L)).thenReturn(List.of(padrao, customizada));

        List<CategoriaResponse> resultado = categoriaService.listarDisponiveisParaUsuario();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).padrao()).isTrue();
        assertThat(resultado.get(1).nome()).isEqualTo("Pets");
        assertThat(resultado.get(1).padrao()).isFalse();
    }
}
