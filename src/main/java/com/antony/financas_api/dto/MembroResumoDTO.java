package com.antony.financas_api.dto;

import java.math.BigDecimal;

// Uma linha do acerto de contas. Saldo positivo = tem a receber; negativo = deve
public record MembroResumoDTO(
        Long usuarioId,
        String login,
        boolean voce,
        BigDecimal totalPago,
        BigDecimal saldo
) {
}
