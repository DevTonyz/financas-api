package com.antony.financas_api.model;

// O Enum é uma lista de valores fixos. Como a forma de o dinheiro sair
// dificilmente muda, deixamos fixo no código para evitar idas ao banco de dados.
public enum FormaPagamento {
    CREDITO,
    DEBITO,
    PIX,
    DINHEIRO
}