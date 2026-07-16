package com.antony.financas_api.model;

import jakarta.persistence.*;

@Entity // 1. Avisa o Spring: "Crie uma tabela no banco de dados para esta classe"
@Table(name = "tb_categorias") // 2. (Opcional) Dá um nome bonitinho para a tabela
public class Categoria {

    @Id // 3. Avisa que o campo abaixo é a Chave Primária (o ID único)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. Faz o banco de dados gerar o ID automaticamente (1, 2, 3...)
    private Long id;

    private String nome;

    // TODO: Gerar os Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}