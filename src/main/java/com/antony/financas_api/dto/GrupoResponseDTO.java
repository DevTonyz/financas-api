package com.antony.financas_api.dto;

import com.antony.financas_api.model.Grupo;

public record GrupoResponseDTO(
        Long id,
        String nome,
        String descricao
) {
    public GrupoResponseDTO(Grupo grupo) {
        this(grupo.getId(), grupo.getNome(), grupo.getDescricao());
    }
}