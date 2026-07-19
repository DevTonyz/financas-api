package com.antony.financas_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotBlank // <-- Coloque aqui, colado em cima da variável 'descricao'
    private String descricao;

    @NotNull  // <-- Coloque aqui
    @Positive // <-- E aqui também, em cima da variável 'valor'
    private BigDecimal valor;

    @NotNull  // <-- Coloque aqui, em cima da variável 'data'
    private LocalDate data;

    @ManyToOne
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    private Integer parcelas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}