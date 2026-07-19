package com.antony.financas_api.dto;

import jakarta.validation.constraints.NotBlank;

public record GrupoRequestDTO(
        @NotBlank String nome,
        String descricao
) {
}