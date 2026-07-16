package com.antony.financas_api.dto;

import com.antony.financas_api.model.FormaPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

// O record já cria tudo (getters, construtores) nos bastidores!
// Reparou que trouxemos as anotações de segurança para cá? A validação agora acontece na porta de entrada!
public record TransacaoRequestDTO(
        @NotBlank String descricao,
        @NotNull @Positive BigDecimal valor,
        @NotNull LocalDate data,
        @NotNull Long categoriaId, // <-- Olha como ficou mais simples para o aplicativo!
        @NotNull FormaPagamento formaPagamento,
        @NotNull Integer parcelas
) {
}