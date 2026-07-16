package com.antony.financas_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ex: "Abastecimento Twister" ou "Mensalidade IFRS"
    private String descricao;

    // Usamos BigDecimal para dinheiro no Java. Ele evita aqueles bugs de
    // dízima periódica (ex: 10.999999) que acontecem com o tipo Double.
    private BigDecimal valor;

    // Para sabermos em qual mês/fatura essa compra entra.
    private LocalDate data;

    // --- RELACIONAMENTOS ---

    // @ManyToOne significa "Muitas transações podem pertencer a Uma mesma categoria".
    // A anotação @JoinColumn diz qual será o nome da coluna de ligação lá no banco.
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // @Enumerated(EnumType.STRING) avisa o banco para salvar a palavra (ex: "CREDITO")
    // em vez de salvar a posição do enum (0, 1, 2). Isso facilita a leitura no banco.
    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    // A quantidade de vezes que a compra foi dividida (1 para débito/pix, 2+ para crédito).
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