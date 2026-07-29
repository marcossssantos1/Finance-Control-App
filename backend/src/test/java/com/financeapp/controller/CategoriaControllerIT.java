package com.financeapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.domain.TipoCategoria;
import com.financeapp.dto.CriarCategoriaRequest;
import com.financeapp.dto.CriarUsuarioRequest;
import com.financeapp.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class CategoriaControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("financeapp_categoria_test")
            .withUsername("financeapp")
            .withPassword("financeapp");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registrarELogar(String email) throws Exception {
        CriarUsuarioRequest registerRequest = new CriarUsuarioRequest("Usuário Teste", email, "senhaForte123");
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        String body = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "senhaForte123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void deveCriarCategoriaCustomizadaEListaLaParaOProprioUsuario() throws Exception {
        String token = registrarELogar("categoria.criar@example.com");
        CriarCategoriaRequest request = new CriarCategoriaRequest("Pets", TipoCategoria.DESPESA, "#9933CC");

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pets"))
                .andExpect(jsonPath("$.padrao").value(false));

        mockMvc.perform(get("/categorias").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Pets')]").isNotEmpty());
    }

    @Test
    void deveListarCategoriasPadraoMesmoSemCategoriasCustomizadas() throws Exception {
        String token = registrarELogar("categoria.padrao@example.com");

        mockMvc.perform(get("/categorias").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Alimentação' && @.padrao == true)]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.nome == 'Salário' && @.padrao == true)]").isNotEmpty());
    }

    @Test
    void naoDeveListarCategoriaCustomizadaDeOutroUsuario() throws Exception {
        String tokenA = registrarELogar("categoria.usuario.a@example.com");
        String tokenB = registrarELogar("categoria.usuario.b@example.com");
        CriarCategoriaRequest request = new CriarCategoriaRequest("Categoria Privada A", TipoCategoria.DESPESA, null);

        mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/categorias").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Categoria Privada A')]").isEmpty())
                .andExpect(jsonPath("$[?(@.padrao == true)]").isNotEmpty());
    }
}
