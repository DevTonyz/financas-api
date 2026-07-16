package com.antony.financas_api.repository;

import com.antony.financas_api.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByCategoriaId(Long id);

    List<Transacao> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);

    // Novo método: Pede para o banco somar (SUM) os valores num período
    @Query("SELECT SUM(t.valor) FROM Transacao t WHERE t.data BETWEEN :inicio AND :fim")
    BigDecimal somarGastosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}