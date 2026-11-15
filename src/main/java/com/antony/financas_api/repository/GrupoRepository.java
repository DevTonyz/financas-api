package com.antony.financas_api.repository;

import com.antony.financas_api.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    // "UsuariosId" navega pela lista de membros: grupos em que esse usuário está
    List<Grupo> findByUsuariosIdOrderByNomeAsc(Long usuarioId);
}
