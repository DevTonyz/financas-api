package com.antony.financas_api.dto;

import java.math.BigDecimal;

public record ResumoResponseDTO(
        Integer mes,
        Integer ano,
        BigDecimal totalGasto
) {
}