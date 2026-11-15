package com.antony.financas_api.exception;

// Lançada quando o pedido bate com algo que já existe (ex.: a pessoa já é membro): o TratadorDeErros transforma em 409
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
