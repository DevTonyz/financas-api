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
        Long grupoId,
        // Parcela de uma compra: qual é (1, 2...) e a descrição sem o " 2/3" do fim. Nulos fora de compra parcelada
        Integer numeroParcela,
        String descricaoBase,
        // Quem lançou: nos gastos de um grupo aparecem transações de vários membros
        Long usuarioId,
        String loginUsuario
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
                transacao.getGrupo() != null ? transacao.getGrupo().getId() : null,
                transacao.getNumeroParcela(),
                transacao.getNumeroParcela() != null ? semSufixoDeParcela(transacao.getDescricao()) : null,
                transacao.getUsuario().getId(),
                transacao.getUsuario().getLogin()
        );
    }

    // "Fone 2/3" -> "Fone"
    public static String semSufixoDeParcela(String descricao) {
        return descricao.replaceFirst("\\s+\\d+/\\d+$", "");
    }
}
