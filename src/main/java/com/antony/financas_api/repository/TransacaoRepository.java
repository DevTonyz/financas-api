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

    Page<Transacao> findAllByUsuarioId(Long usuarioId, Pageable paginacao);

    Page<Transacao> findByCategoriaIdAndUsuarioId(Long categoriaId, Long usuarioId, Pageable paginacao);

    Page<Transacao> findByDataBetweenAndUsuarioId(LocalDate dataInicio, LocalDate dataFim, Long usuarioId, Pageable paginacao);

    @Query("SELECT SUM(t.valor) FROM Transacao t WHERE t.usuario.id = :usuarioId AND t.data BETWEEN :inicio AND :fim")
    BigDecimal somarGastosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("usuarioId") Long usuarioId);
}