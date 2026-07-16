package com.antony.financas_api.dto;

import com.antony.financas_api.model.Transacao;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        @JsonFormat(pattern = "dd/MM/yyyy") // <-- Mágica da formatação de data!
        LocalDate data,
        String nomeCategoria // Trazemos o nome limpo em vez de um objeto complexo
) {
    // Este construtor funciona como um "tradutor".
    // Ele pega a Entidade pesada do banco e extrai apenas o suco para a internet.
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