package com.antony.financas_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_categorias")
@Getter
@Setter
public class Categoria {

    @Id // 3. Avisa que o campo abaixo é a Chave Primária (o ID único)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. Faz o banco de dados gerar o ID automaticamente (1, 2, 3...)
    private Long id;

    private String nome;

    // Dono da categoria. Pode ser nulo só para não quebrar categorias antigas, criadas antes de existir dono.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
