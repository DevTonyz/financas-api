package com.antony.financas_api.repository;

import com.antony.financas_api.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findAllByGrupoId(Long grupoId);

    List<Pagamento> findAllByGrupoIdAndDataBetweenOrderByDataDescIdDesc(Long grupoId, LocalDate inicio, LocalDate fim);

    // Quem sai do grupo: os acertos em que ele aparece deixam de valer
    @Modifying
    @Query("DELETE FROM Pagamento p WHERE p.grupo.id = :grupoId AND (p.pagador.id = :usuarioId OR p.recebedor.id = :usuarioId)")
    int apagarDoMembro(@Param("grupoId") Long grupoId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("DELETE FROM Pagamento p WHERE p.grupo.id = :grupoId")
    int apagarDoGrupo(@Param("grupoId") Long grupoId);
}
