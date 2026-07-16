package com.antony.financas_api.repository;

import com.antony.financas_api.model.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    // Agora devolvemos "Páginas" em vez de "Listas"
    Page<Transacao> findByCategoriaId(Long id, Pageable paginacao);

    Page<Transacao> findByDataBetween(LocalDate dataInicio, LocalDate dataFim, Pageable paginacao);

    @Query("SELECT SUM(t.valor) FROM Transacao t WHERE t.data BETWEEN :inicio AND :fim")
    BigDecimal somarGastosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}