package com.antony.financas_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Table(name = "grupos")
@Entity(name = "Grupo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;

    // Aqui acontece a mágica do relacionamento
    @ManyToMany
    @JoinTable(
            name = "grupo_usuarios", // Nome da tabela intermediária que será criada
            joinColumns = @JoinColumn(name = "grupo_id"), // Chave estrangeira deste lado (Grupo)
            inverseJoinColumns = @JoinColumn(name = "usuario_id") // Chave estrangeira do outro lado (Usuario)
    )
    private Set<Usuario> usuarios = new HashSet<>();
}