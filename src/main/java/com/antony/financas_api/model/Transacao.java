package com.antony.financas_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "tb_transacoes")
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


    // --- GETTERS E SETTERS ---
    // (Pode gerar automaticamente pela IDE com Alt + Insert ou botão direito -> Generate)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
}