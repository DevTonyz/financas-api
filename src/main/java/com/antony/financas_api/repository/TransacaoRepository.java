package com.antony.financas_api.repository;

import com.antony.financas_api.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    // O Spring lê "findBy" (Buscar por) + "CategoriaId" e monta o SQL sozinho!
    List<Transacao> findByCategoriaId(Long id);

}