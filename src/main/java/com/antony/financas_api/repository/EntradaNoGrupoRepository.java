package com.antony.financas_api.repository;

import com.antony.financas_api.model.EntradaNoGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntradaNoGrupoRepository extends JpaRepository<EntradaNoGrupo, Long> {

    List<EntradaNoGrupo> findAllByGrupoId(Long grupoId);

    @Modifying
    @Query("DELETE FROM EntradaNoGrupo e WHERE e.grupoId = :grupoId AND e.usuarioId = :usuarioId")
    int apagarDoMembro(@Param("grupoId") Long grupoId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("DELETE FROM EntradaNoGrupo e WHERE e.grupoId = :grupoId")
    int apagarDoGrupo(@Param("grupoId") Long grupoId);
}
