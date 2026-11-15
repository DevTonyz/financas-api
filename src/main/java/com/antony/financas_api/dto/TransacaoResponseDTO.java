package com.antony.financas_api.dto;

import com.antony.financas_api.model.FormaPagamento;
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
        String nomeCategoria,
        // Campos extras para a tela de edição do app conseguir preencher o formulário
        Long categoriaId,
        FormaPagamento formaPagamento,
        Integer parcelas,
        Long grupoId
) {
    public TransacaoResponseDTO(Transacao transacao) {
        this(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getData(),
                transacao.getCategoria().getNome(),
                transacao.getCategoria().getId(),
                transacao.getFormaPagamento(),
                transacao.getParcelas(),
                transacao.getGrupo() != null ? transacao.getGrupo().getId() : null
        );
    }
}
