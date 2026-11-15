package com.antony.financas_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_transacoes")
@Getter
@Setter
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private BigDecimal valor;

    private LocalDate data;

    @ManyToOne
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    private Integer parcelas;

    // Liga as parcelas de uma mesma compra no crédito (todas têm o mesmo compraId).
    // Nulo em gastos à vista e nas transações antigas, criadas antes desse campo
    private String compraId;

    // Qual parcela é esta (1, 2, 3...). Nulo quando não faz parte de uma compra parcelada
    private Integer numeroParcela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // NOVO: Relacionamento opcional com o Grupo (Cofre da casa)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;
}