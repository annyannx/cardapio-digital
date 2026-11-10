package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.AuthDtos.LoginRequest;
import com.cardapio.cardapio_api.dto.AuthDtos.LoginResponse;
import com.cardapio.cardapio_api.model.Usuario;
import com.cardapio.cardapio_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Regras do login do administrador (RF005).
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        // A mesma mensagem para "e-mail não existe" e "senha errada":
        // assim ninguém descobre quais e-mails estão cadastrados.
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(Usuario::isAtivo)
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiracaoSegundos(),
                usuario.getNome(),
                usuario.getPerfil());
    }
}
