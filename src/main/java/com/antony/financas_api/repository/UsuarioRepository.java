package com.antony.financas_api.repository;

import com.antony.financas_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // O Spring Data JPA é tão inteligente que ele monta o comando SQL
    // "SELECT * FROM usuarios WHERE login = ?" só de ler o nome deste método!
    UserDetails findByLogin(String login);
}