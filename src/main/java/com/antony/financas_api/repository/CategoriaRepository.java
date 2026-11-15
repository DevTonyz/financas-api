package com.antony.financas_api.repository;

import com.antony.financas_api.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByUsuarioIdOrderByNomeAsc(Long usuarioId);

    // Só acha a categoria se ela for do usuário: a de outra pessoa é tratada como inexistente (404)
    Optional<Categoria> findByIdAndUsuarioId(Long id, Long usuarioId);
}
