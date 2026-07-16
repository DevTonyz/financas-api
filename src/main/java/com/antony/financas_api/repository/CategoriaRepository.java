package com.antony.financas_api.repository;

import com.antony.financas_api.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

// O JpaRepository recebe dois parâmetros: A classe que ele vai gerenciar (Categoria)
// e o tipo da Chave Primária dela (Long do nosso id).
// Só de herdar (extends) essa classe, já ganhamos os métodos save(), findAll(), deleteById()...
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}