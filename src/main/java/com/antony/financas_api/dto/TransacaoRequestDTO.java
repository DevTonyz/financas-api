package com.antony.financas_api.dto;

import com.antony.financas_api.model.FormaPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequestDTO(
        @NotBlank String descricao,
        @NotNull @Positive BigDecimal valor,
        @NotNull LocalDate data,
        @NotNull Long categoriaId,
        @NotNull FormaPagamento formaPagamento,
        @NotNull Integer parcelas
) {
}