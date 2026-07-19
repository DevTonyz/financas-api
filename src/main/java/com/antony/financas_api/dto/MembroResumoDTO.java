package com.antony.financas_api.dto;

import java.math.BigDecimal;

public record MembroResumoDTO(
        Long usuarioId,
        String nome,
        BigDecimal totalPago
) {
}