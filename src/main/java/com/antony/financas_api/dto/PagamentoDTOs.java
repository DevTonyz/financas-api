package com.antony.financas_api.dto;

import com.antony.financas_api.model.Pagamento;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// DTOs do acerto acumulado: pagamentos entre membros e o saldo geral do grupo
public final class PagamentoDTOs {

    private PagamentoDTOs() {
    }

    // Corpo de POST /grupos/{id}/pagamentos. Sem data = hoje. A data ENTRA em "yyyy-MM-dd"
    public record PagamentoRequest(
            @NotNull Long deUsuarioId,
            @NotNull Long paraUsuarioId,
            @NotNull @Positive BigDecimal valor,
            LocalDate data
    ) {
    }

    // A data SAI em "dd/MM/yyyy", igual às transações
    public record PagamentoResponse(
            Long id,
            Long deUsuarioId,
            String deLogin,
            Long paraUsuarioId,
            String paraLogin,
            BigDecimal valor,
            @JsonFormat(pattern = "dd/MM/yyyy") LocalDate data
    ) {
        public PagamentoResponse(Pagamento p) {
            this(p.getId(), p.getPagador().getId(), p.getPagador().getLogin(),
                    p.getRecebedor().getId(), p.getRecebedor().getLogin(), p.getValor(), p.getData());
        }
    }

    // GET /grupos/{id}/saldos: o saldo de cada membro somando todos os meses e os pagamentos já feitos
    public record SaldosResponse(
            List<SaldoDoMembro> membros,
            // Quem paga quanto a quem para todo mundo ficar quite (o menor número de pagamentos)
            List<Sugestao> sugestoes
    ) {
    }

    // Saldo > 0 = tem a receber; < 0 = deve
    public record SaldoDoMembro(Long usuarioId, String login, boolean voce, BigDecimal saldo) {
    }

    public record Sugestao(Long deUsuarioId, String deLogin, Long paraUsuarioId, String paraLogin, BigDecimal valor) {
    }
}
