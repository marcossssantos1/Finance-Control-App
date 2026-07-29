package com.financeapp.controller;

import com.financeapp.dto.ContaResponse;
import com.financeapp.dto.CriarContaRequest;
import com.financeapp.dto.SaldoContaResponse;
import com.financeapp.domain.Conta;
import com.financeapp.service.ContaService;
import com.financeapp.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService contaService;
    private final TransacaoService transacaoService;

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody CriarContaRequest request) {
        ContaResponse response = contaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ContaResponse>> listar() {
        return ResponseEntity.ok(contaService.listarDoUsuarioLogado());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        ContaResponse response = ContaResponse.fromEntity(contaService.buscarPorIdEValidarDono(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity<SaldoContaResponse> saldo(@PathVariable Long id) {
        // calcularSaldoConta já valida, internamente, que a conta pertence
        // ao usuário logado (via ContaService.buscarPorIdEValidarDono).
        Conta conta = contaService.buscarPorIdEValidarDono(id);
        BigDecimal saldoAtual = transacaoService.calcularSaldoConta(id);
        return ResponseEntity.ok(new SaldoContaResponse(conta.getId(), conta.getNome(), saldoAtual));
    }
}
