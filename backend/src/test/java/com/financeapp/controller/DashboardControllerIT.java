package com.financeapp.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class DashboardControllerIT {

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

    private Long criarConta(String token, BigDecimal saldoInicial) throws Exception {
        var request = new CriarContaRequest("Nubank", TipoConta.CORRENTE, saldoInicial);
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

    private void criarTransacao(String token, Long contaId, Long categoriaId, TipoCategoria tipo,
                                 BigDecimal valor, LocalDate data) throws Exception {
        var request = new CriarTransacaoRequest("Transação teste", valor, tipo, contaId, categoriaId, data);
        mockMvc.perform(post("/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveCalcularResumoDoDashboardCorretamente() throws Exception {
        String token = registrarELogar("dashboard.resumo@example.com");
        Long contaId = criarConta(token, BigDecimal.ZERO);
        Long categoriaReceita = criarCategoria(token, "Salário Custom", TipoCategoria.RECEITA);
        Long categoriaDespesa = criarCategoria(token, "Mercado Custom", TipoCategoria.DESPESA);

        LocalDate hoje = LocalDate.now();
        criarTransacao(token, contaId, categoriaReceita, TipoCategoria.RECEITA, new BigDecimal("2000.00"), hoje);
        criarTransacao(token, contaId, categoriaDespesa, TipoCategoria.DESPESA, new BigDecimal("300.00"), hoje);
        criarTransacao(token, contaId, categoriaDespesa, TipoCategoria.DESPESA, new BigDecimal("150.00"), hoje);

        mockMvc.perform(get("/dashboard/resumo")
                        .header("Authorization", "Bearer " + token)
                        .param("dataInicio", hoje.minusDays(1).toString())
                        .param("dataFim", hoje.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitas").value(2000.00))
                .andExpect(jsonPath("$.totalDespesas").value(450.00))
                .andExpect(jsonPath("$.saldoPeriodo").value(1550.00))
                .andExpect(jsonPath("$.porCategoria.length()").value(2));
    }

    @Test
    void resumoDeveExcluirTransacoesForaDoPeriodo() throws Exception {
        String token = registrarELogar("dashboard.periodo@example.com");
        Long contaId = criarConta(token, BigDecimal.ZERO);
        Long categoriaDespesa = criarCategoria(token, "Categoria Antiga", TipoCategoria.DESPESA);

        criarTransacao(token, contaId, categoriaDespesa, TipoCategoria.DESPESA,
                new BigDecimal("999.00"), LocalDate.now().minusMonths(3));

        mockMvc.perform(get("/dashboard/resumo")
                        .header("Authorization", "Bearer " + token)
                        .param("dataInicio", LocalDate.now().minusDays(1).toString())
                        .param("dataFim", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDespesas").value(0))
                .andExpect(jsonPath("$.porCategoria.length()").value(0));
    }

    @Test
    void deveCalcularSaldoDaConta() throws Exception {
        String token = registrarELogar("saldo.conta@example.com");
        Long contaId = criarConta(token, new BigDecimal("100.00"));
        Long categoriaReceita = criarCategoria(token, "Receita Saldo", TipoCategoria.RECEITA);
        Long categoriaDespesa = criarCategoria(token, "Despesa Saldo", TipoCategoria.DESPESA);

        criarTransacao(token, contaId, categoriaReceita, TipoCategoria.RECEITA, new BigDecimal("500.00"), LocalDate.now());
        criarTransacao(token, contaId, categoriaDespesa, TipoCategoria.DESPESA, new BigDecimal("200.00"), LocalDate.now());

        // saldo esperado: 100 (inicial) + 500 (receita) - 200 (despesa) = 400
        mockMvc.perform(get("/contas/" + contaId + "/saldo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAtual").value(400.00));
    }

    @Test
    void naoDeveVerSaldoDeContaDeOutroUsuario() throws Exception {
        String tokenA = registrarELogar("saldo.dono.a@example.com");
        String tokenB = registrarELogar("saldo.dono.b@example.com");

        Long contaDeA = criarConta(tokenA, new BigDecimal("50.00"));

        mockMvc.perform(get("/contas/" + contaDeA + "/saldo")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }
}
