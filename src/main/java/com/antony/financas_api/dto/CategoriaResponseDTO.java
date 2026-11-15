package com.antony.financas_api.dto;

import com.antony.financas_api.model.Categoria;

public record CategoriaResponseDTO(Long id, String nome) {
    public CategoriaResponseDTO(Categoria categoria) {
        this(categoria.getId(), categoria.getNome());
    }
}
