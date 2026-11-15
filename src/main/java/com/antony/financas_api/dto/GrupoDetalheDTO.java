package com.antony.financas_api.dto;

import com.antony.financas_api.model.Grupo;

import java.util.Comparator;
import java.util.List;

// Resposta de GET /grupos/{id}: o grupo e seus membros, em ordem de e-mail
public record GrupoDetalheDTO(
        Long id,
        String nome,
        String descricao,
        List<MembroDTO> membros
) {
    public GrupoDetalheDTO(Grupo grupo, Long idUsuarioLogado) {
        this(grupo.getId(), grupo.getNome(), grupo.getDescricao(),
                grupo.getUsuarios().stream()
                        .sorted(Comparator.comparing(u -> u.getLogin()))
                        .map(u -> new MembroDTO(u.getId(), u.getLogin(), u.getId().equals(idUsuarioLogado)))
                        .toList());
    }

    // "voce" marca o usuário logado, para o app mostrar "(você)" e saber quais gastos são dele
    public record MembroDTO(Long id, String login, boolean voce) {
    }
}
