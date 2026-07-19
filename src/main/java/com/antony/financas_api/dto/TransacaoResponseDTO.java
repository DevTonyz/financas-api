package com.antony.financas_api.dto;

import com.antony.financas_api.model.Transacao;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate data,
        String nomeCategoria
) {
    public TransacaoResponseDTO(Transacao transacao) {
        this(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getData(),
                transacao.getCategoria().getNome()
        );
    }
}