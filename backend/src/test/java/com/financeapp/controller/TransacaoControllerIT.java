package com.financeapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.domain.TipoCategoria;
import com.financeapp.domain.TipoConta;
import com.financeapp.dto.CriarCategoriaRequest;
import com.financeapp.dto.CriarContaRequest;
import com.financeapp.dto.CriarTransacaoRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class TransacaoControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("financeapp_test")
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
        var registerRequest = new CriarUsuarioRequest("Usuário Teste", email, "senhaForte123");
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        var loginRequest = new LoginRequest(email, "senhaForte123");
        String body = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    private Long criarConta(String token) throws Exception {
        var request = new CriarContaRequest("Nubank", TipoConta.CORRENTE, new BigDecimal("1000.00"));
        String body = mockMvc.perform(post("/contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long criarCategoria(String token, String nome, TipoCategoria tipo) throws Exception {
        var request = new CriarCategoriaRequest(nome, tipo, null);
        String body = mockMvc.perform(post("/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void deveCriarELisarTransacao() throws Exception {
        String token = registrarELogar("fluxo.completo@example.com");
        Long contaId = criarConta(token);
        Long categoriaId = criarCategoria(token, "Alimentação Custom", TipoCategoria.DESPESA);

        var request = new CriarTransacaoRequest(
                "Mercado", new BigDecimal("120.50"), TipoCategoria.DESPESA, contaId, categoriaId, LocalDate.now()
        );

        mockMvc.perform(post("/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Mercado"))
                .andExpect(jsonPath("$.contaNome").value("Nubank"));

        mockMvc.perform(get("/transacoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void naoDeveCriarTransacaoComTipoDivergenteDaCategoria() throws Exception {
        String token = registrarELogar("tipo.divergente@example.com");
        Long contaId = criarConta(token);
        Long categoriaId = criarCategoria(token, "Salário Custom", TipoCategoria.RECEITA);

        var request = new CriarTransacaoRequest(
                "Errado", new BigDecimal("50.00"), TipoCategoria.DESPESA, contaId, categoriaId, LocalDate.now()
        );

        mockMvc.perform(post("/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void naoDeveCriarTransacaoComContaOuCategoriaDeOutroUsuario() throws Exception {
        String tokenA = registrarELogar("dono.a@example.com");
        String tokenB = registrarELogar("dono.b@example.com");

        Long contaDeA = criarConta(tokenA);
        Long categoriaDeA = criarCategoria(tokenA, "Categoria de A", TipoCategoria.DESPESA);
        Long contaDeB = criarConta(tokenB);

        // B tenta lançar transação usando a conta de A -> 404
        var requestComContaAlheia = new CriarTransacaoRequest(
                "Tentativa", new BigDecimal("10.00"), TipoCategoria.DESPESA, contaDeA, categoriaDeA, LocalDate.now()
        );
        mockMvc.perform(post("/transacoes")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestComContaAlheia)))
                .andExpect(status().isNotFound());

        // B tenta lançar transação usando a categoria customizada de A -> 404
        var requestComCategoriaAlheia = new CriarTransacaoRequest(
                "Tentativa", new BigDecimal("10.00"), TipoCategoria.DESPESA, contaDeB, categoriaDeA, LocalDate.now()
        );
        mockMvc.perform(post("/transacoes")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestComCategoriaAlheia)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveFiltrarPorPeriodo() throws Exception {
        String token = registrarELogar("filtro.periodo@example.com");
        Long contaId = criarConta(token);
        Long categoriaId = criarCategoria(token, "Categoria Filtro", TipoCategoria.DESPESA);

        criarTransacao(token, contaId, categoriaId, "Antiga", LocalDate.now().minusMonths(2));
        criarTransacao(token, contaId, categoriaId, "Recente", LocalDate.now());

        mockMvc.perform(get("/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .param("dataInicio", LocalDate.now().minusDays(1).toString())
                        .param("dataFim", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].descricao").value("Recente"));
    }

    @Test
    void devePaginarResultados() throws Exception {
        String token = registrarELogar("paginacao@example.com");
        Long contaId = criarConta(token);
        Long categoriaId = criarCategoria(token, "Categoria Paginacao", TipoCategoria.DESPESA);

        for (int i = 0; i < 5; i++) {
            criarTransacao(token, contaId, categoriaId, "Item " + i, LocalDate.now());
        }

        mockMvc.perform(get("/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(5));

        mockMvc.perform(get("/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void deveExcluirTransacao() throws Exception {
        String token = registrarELogar("exclusao@example.com");
        Long contaId = criarConta(token);
        Long categoriaId = criarCategoria(token, "Categoria Exclusao", TipoCategoria.DESPESA);

        Long transacaoId = criarTransacao(token, contaId, categoriaId, "Para excluir", LocalDate.now());

        mockMvc.perform(delete("/transacoes/" + transacaoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/transacoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    private Long criarTransacao(String token, Long contaId, Long categoriaId, String descricao, LocalDate data) throws Exception {
        var request = new CriarTransacaoRequest(
                descricao, new BigDecimal("10.00"), TipoCategoria.DESPESA, contaId, categoriaId, data
        );
        String body = mockMvc.perform(post("/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        return json.get("id").asLong();
    }
}
