package com.financeapp.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException conta(Long id) {
        return new RecursoNaoEncontradoException("Conta não encontrada: " + id);
    }
}
