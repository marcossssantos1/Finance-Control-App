package com.financeapp.exception;

public class TipoTransacaoInvalidoException extends RuntimeException {

    public TipoTransacaoInvalidoException(String mensagem) {
        super(mensagem);
    }

    public static TipoTransacaoInvalidoException tipoDivergenteDaCategoria() {
        return new TipoTransacaoInvalidoException(
                "O tipo da transação não bate com o tipo da categoria selecionada"
        );
    }
}
