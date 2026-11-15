package com.antony.financas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GrupoRequestDTO(
        @NotBlank @Size(max = 60) String nome,
        @Size(max = 255) String descricao
) {
}
