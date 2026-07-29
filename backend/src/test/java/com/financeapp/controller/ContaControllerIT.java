package com.financeapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.dto.CriarContaRequest;
import com.financeapp.dto.CriarUsuarioRequest;
import com.financeapp.domain.TipoConta;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class ContaControllerIT {

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

        var loginRequest = new com.financeapp.dto.LoginRequest(email, "senhaForte123");
        String body = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void deveCriarEListarContasDoUsuarioLogado() throws Exception {
        String token = registrarELogar("usuario.contas@example.com");

        var criarContaRequest = new CriarContaRequest("Nubank", TipoConta.CORRENTE, new BigDecimal("500.00"));

        mockMvc.perform(post("/contas")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Nubank"))
                .andExpect(jsonPath("$.saldoInicial").value(500.00));

        mockMvc.perform(get("/contas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Nubank"));
    }

    @Test
    void naoDeveCriarContaSemAutenticacao() throws Exception {
        var criarContaRequest = new CriarContaRequest("Carteira", TipoConta.CARTEIRA, null);

        mockMvc.perform(post("/contas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void naoDeveAcessarContaDeOutroUsuario() throws Exception {
        String tokenUsuarioA = registrarELogar("usuario.a@example.com");
        String tokenUsuarioB = registrarELogar("usuario.b@example.com");

        var criarContaRequest = new CriarContaRequest("Conta Secreta A", TipoConta.POUPANCA, BigDecimal.TEN);

        String responseBody = mockMvc.perform(post("/contas")
                        .header("Authorization", "Bearer " + tokenUsuarioA)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long contaIdDoUsuarioA = objectMapper.readTree(responseBody).get("id").asLong();

        // usuário B tenta acessar a conta do usuário A -> deve ser 404, não 403
        // (não revela se o recurso existe, apenas nega o acesso)
        mockMvc.perform(get("/contas/" + contaIdDoUsuarioA)
                        .header("Authorization", "Bearer " + tokenUsuarioB))
                .andExpect(status().isNotFound());

        // usuário B não vê a conta do usuário A na própria listagem
        mockMvc.perform(get("/contas")
                        .header("Authorization", "Bearer " + tokenUsuarioB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // o próprio dono acessa normalmente
        mockMvc.perform(get("/contas/" + contaIdDoUsuarioA)
                        .header("Authorization", "Bearer " + tokenUsuarioA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Conta Secreta A"));
    }
}
