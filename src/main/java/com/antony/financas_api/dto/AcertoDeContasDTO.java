package com.antony.financas_api.dto;

import java.math.BigDecimal;
import java.util.List;

// Resposta de GET /transacoes/grupo/{id}/acerto: quanto cada membro pagou e quanto falta ou sobra
public record AcertoDeContasDTO(
        Integer mes,
        Integer ano,
        BigDecimal total,
        // Total dividido pelo número de membros (arredondado em centavos)
        BigDecimal parteJusta,
        List<MembroResumoDTO> membros
) {
}
