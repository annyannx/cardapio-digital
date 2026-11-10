package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acesso ao banco para usuários.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca pelo e-mail sem diferenciar maiúsculas de minúsculas.
    Optional<Usuario> findByEmailIgnoreCase(String email);
}
