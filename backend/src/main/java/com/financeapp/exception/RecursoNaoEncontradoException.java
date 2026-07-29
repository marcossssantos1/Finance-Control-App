package com.financeapp.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException conta(Long id) {
        return new RecursoNaoEncontradoException("Conta não encontrada: " + id);
    }

    public static RecursoNaoEncontradoException categoria(Long id) {
        return new RecursoNaoEncontradoException("Categoria não encontrada ou não disponível: " + id);
    }

    public static RecursoNaoEncontradoException transacao(Long id) {
        return new RecursoNaoEncontradoException("Transação não encontrada: " + id);
    }
}
