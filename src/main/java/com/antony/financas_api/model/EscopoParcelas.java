package com.antony.financas_api.model;

// A quais parcelas de uma compra uma edição ou exclusão se aplica
public enum EscopoParcelas {
    ESTA,      // só a parcela escolhida
    PROXIMAS,  // a escolhida e as seguintes
    TODAS      // a compra inteira (só na exclusão)
}
