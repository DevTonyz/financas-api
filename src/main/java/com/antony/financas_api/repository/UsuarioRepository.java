package com.antony.financas_api.repository;

import com.antony.financas_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    UserDetails findByLogin(String login);

    // O Spring Data monta a consulta sozinho a partir do nome do método
    boolean existsByLogin(String login);

    // Usado para adicionar membro ao grupo pelo e-mail, sem diferenciar maiúsculas
    Optional<Usuario> findFirstByLoginIgnoreCase(String login);
}
