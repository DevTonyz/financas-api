package com.antony.financas_api.dto;

import com.antony.financas_api.model.Grupo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// Resposta de GET /grupos/{id}: o grupo e seus membros, em ordem de e-mail
public record GrupoDetalheDTO(
        Long id,
        String nome,
        String descricao,
        List<MembroDTO> membros
) {
    // entradas: id do usuário → data em que entrou (quem não está no mapa entrou antes dessa regra existir)
    public GrupoDetalheDTO(Grupo grupo, Long idUsuarioLogado, Map<Long, LocalDate> entradas) {
        this(grupo.getId(), grupo.getNome(), grupo.getDescricao(),
                grupo.getUsuarios().stream()
                        .sorted(Comparator.comparing(u -> u.getLogin()))
                        .map(u -> new MembroDTO(u.getId(), u.getLogin(), u.getId().equals(idUsuarioLogado),
                                entradas.get(u.getId())))
                        .toList());
    }

    // "voce" marca o usuário logado, para o app mostrar "(você)" e saber quais gastos são dele.
    // dataEntrada sai como "yyyy-MM-dd" e é nula para quem já era membro antes da regra (divide tudo)
    public record MembroDTO(Long id, String login, boolean voce, LocalDate dataEntrada) {
    }
}
