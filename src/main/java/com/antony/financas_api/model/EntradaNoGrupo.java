package com.antony.financas_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// Quando cada pessoa entrou num grupo. Serve para quem chega não dividir os gastos de antes da entrada.
// Fica numa tabela à parte porque a grupo_usuarios é gerada pelo @ManyToMany e não aceita colunas extras.
// Membro sem registro aqui (os que já estavam antes desta regra) conta como "desde sempre"
@Entity
@Table(name = "tb_entradas_grupo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"grupo_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
public class EntradaNoGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grupo_id", nullable = false)
    private Long grupoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;

    public EntradaNoGrupo(Long grupoId, Long usuarioId, LocalDate dataEntrada) {
        this.grupoId = grupoId;
        this.usuarioId = usuarioId;
        this.dataEntrada = dataEntrada;
    }
}
