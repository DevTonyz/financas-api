package com.antony.financas_api.exception;

// Lançada quando o recurso existe mas pertence a outra pessoa: o TratadorDeErros transforma em 403
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
