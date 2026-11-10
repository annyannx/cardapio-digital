package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Gera os tokens JWT assinados (HS256) entregues no login.
 *
 * O token carrega: e-mail (subject), id, nome, perfis ("roles"),
 * data de emissão e data de expiração.
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expiracaoMinutos;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${app.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.jwtEncoder = jwtEncoder;
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cardapio-api")
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES))
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("roles", List.of(usuario.getPerfil()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpiracaoSegundos() {
        return expiracaoMinutos * 60;
    }
}
