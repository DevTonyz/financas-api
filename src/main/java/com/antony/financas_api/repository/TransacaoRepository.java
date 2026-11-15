package com.antony.financas_api.repository;

import com.antony.financas_api.model.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Page<Transacao> findAllByUsuarioId(Long usuarioId, Pageable paginacao);

    boolean existsByCategoriaId(Long categoriaId);

    // Parcelas de uma compra a partir de um número (inclusive), em ordem: usada no "esta e as próximas"
    java.util.List<Transacao> findByCompraIdAndNumeroParcelaGreaterThanEqualOrderByNumeroParcela(String compraId, Integer numeroParcela);

    Page<Transacao> findByCategoriaIdAndUsuarioId(Long categoriaId, Long usuarioId, Pageable paginacao);

    Page<Transacao> findByDataBetweenAndUsuarioId(LocalDate dataInicio, LocalDate dataFim, Long usuarioId, Pageable paginacao);

    Page<Transacao> findByGrupoId(Long grupoId, Pageable paginacao);

    Page<Transacao> findByGrupoIdAndDataBetween(Long grupoId, LocalDate inicio, LocalDate fim, Pageable paginacao);

    // @Modifying: é um UPDATE, não uma consulta. Tira do grupo os gastos de quem saiu dele
    @Modifying
    @Query("UPDATE Transacao t SET t.grupo = null WHERE t.grupo.id = :grupoId AND t.usuario.id = :usuarioId")
    int desligarDoGrupo(@Param("grupoId") Long grupoId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("UPDATE Transacao t SET t.grupo = null WHERE t.grupo.id = :grupoId")
    int desligarTodasDoGrupo(@Param("grupoId") Long grupoId);

    // NOVO: Busca a lista completa de transações de um grupo em um determinado período
    java.util.List<Transacao> findAllByGrupoIdAndDataBetween(Long grupoId, java.time.LocalDate dataInicio, java.time.LocalDate dataFim);

    @Query("SELECT SUM(t.valor) FROM Transacao t WHERE t.usuario.id = :usuarioId AND t.data BETWEEN :inicio AND :fim")
    BigDecimal somarGastosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("usuarioId") Long usuarioId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(t.valor) FROM Transacao t WHERE t.grupo.id = :grupoId AND t.data >= :dataInicio AND t.data <= :dataFim")
    java.math.BigDecimal somarGastosPorGrupoEPeriodo(
            @org.springframework.data.repository.query.Param("grupoId") Long grupoId,
            @org.springframework.data.repository.query.Param("dataInicio") java.time.LocalDate dataInicio,
            @org.springframework.data.repository.query.Param("dataFim") java.time.LocalDate dataFim
    );
}