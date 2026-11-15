package com.antony.financas_api.service;

import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Calcula a parte justa de cada membro levando em conta quando ele entrou no grupo.
// Regra: um gasto é dividido entre quem já era membro na data dele (entrou nesse dia ou antes),
// mais quem pagou (se alguém lançar um gasto com data anterior à própria entrada, ele também divide).
// Ex.: A e B dividem tudo desde 01/10; C entra em 10/11. Um gasto de 05/11 fica só entre A e B;
// um de 12/11, entre os três.
final class DivisaoPorEntrada {

    private DivisaoPorEntrada() {
    }

    /**
     * @param membros  membros atuais, em ordem de e-mail (a ordem decide quem leva os centavos)
     * @param entradas id do usuário → data de entrada; quem não está aqui conta como "desde sempre"
     * @return id do usuário → parte justa somada. A soma das partes é sempre o total dos gastos
     */
    static Map<Long, BigDecimal> partes(List<Usuario> membros, Map<Long, LocalDate> entradas, List<Transacao> gastos) {
        // Junta os gastos pelo conjunto de pessoas que dividem cada um e só depois divide em centavos:
        // assim os centavos não se perdem gasto a gasto e os saldos continuam somando exatamente 0
        Map<List<Long>, BigDecimal> totalPorConjunto = new LinkedHashMap<>();
        for (Transacao t : gastos) {
            List<Long> quemDivide = new ArrayList<>();
            for (Usuario m : membros) {
                LocalDate entrada = entradas.get(m.getId());
                boolean jaEstava = entrada == null || !entrada.isAfter(t.getData());
                if (jaEstava || m.getId().equals(t.getUsuario().getId())) {
                    quemDivide.add(m.getId());
                }
            }
            if (quemDivide.isEmpty()) {
                continue; // não acontece: quem lançou sempre é membro (quem sai leva os gastos dele)
            }
            totalPorConjunto.merge(quemDivide, t.getValor(), BigDecimal::add);
        }

        Map<Long, BigDecimal> parte = new HashMap<>();
        membros.forEach(m -> parte.put(m.getId(), BigDecimal.ZERO));
        totalPorConjunto.forEach((quemDivide, total) -> {
            List<BigDecimal> divisao = Divisao.emPartes(total, quemDivide.size());
            for (int i = 0; i < quemDivide.size(); i++) {
                parte.merge(quemDivide.get(i), divisao.get(i), BigDecimal::add);
            }
        });
        return parte;
    }
}
