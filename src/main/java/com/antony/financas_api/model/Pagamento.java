package com.antony.financas_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

// Um acerto entre dois membros de um grupo ("fulano pagou R$ 30 para ciclano").
// Não é um gasto: não entra no total do grupo, só mexe nos saldos
@Entity
@Table(name = "tb_pagamentos")
@Getter
@Setter
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    // Quem pagou (o saldo dele sobe) e quem recebeu (o saldo dele desce)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagador_id", nullable = false)
    private Usuario pagador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recebedor_id", nullable = false)
    private Usuario recebedor;

    private BigDecimal valor;

    private LocalDate data;
}
