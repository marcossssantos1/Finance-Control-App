package com.financeapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class AuthControllerIT {

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

    @Test
    void deveRegistrarLogarEAcessarRotaProtegidaComToken() throws Exception {
        String email = "teste.integracao@example.com";
        var registerRequest = new CriarUsuarioRequest("Usuário Teste", email, "senhaForte123");

        // registro
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));

        // login
        var loginRequest = new LoginRequest(email, "senhaForte123");
        String responseBody = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(responseBody).get("token").asText();

        // acesso negado sem token a uma rota que não está na whitelist pública
        // (o SecurityFilterChain barra antes mesmo de resolver o handler, retornando 401)
        mockMvc.perform(get("/api/qualquer-rota-protegida"))
                .andExpect(status().isUnauthorized());

        // acesso autenticado com token válido: passa pelo filtro de segurança;
        // retorna 404 pois o endpoint em si ainda não existe (será criado na Onda 2)
        mockMvc.perform(get("/api/qualquer-rota-protegida")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // login com senha errada -> 401
        var loginInvalido = new LoginRequest(email, "senhaErrada123");
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginInvalido)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotasPublicasDevemFuncionarSemAutenticacao() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk());
    }
}
