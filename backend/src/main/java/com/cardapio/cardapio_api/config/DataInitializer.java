package com.cardapio.cardapio_api.config;

import com.cardapio.cardapio_api.model.Usuario;
import com.cardapio.cardapio_api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Ao ligar o sistema, cria o usuário administrador inicial caso ele ainda não exista.
 * A senha é guardada como hash BCrypt (nunca em texto puro).
 *
 * E-mail e senha vêm de app.admin.* (variáveis ADMIN_EMAIL e ADMIN_PASSWORD).
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner criarAdminInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.senha}") String senha,
            @Value("${app.admin.nome}") String nome) {

        return args -> {
            if (usuarioRepository.findByEmailIgnoreCase(email).isEmpty()) {
                usuarioRepository.save(new Usuario(nome, email, passwordEncoder.encode(senha), "ADMIN"));
                log.info("Usuário administrador criado: {}", email);
            }
        };
    }
}
