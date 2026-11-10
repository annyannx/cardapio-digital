package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.AuthDtos.UsuarioLogadoResponse;
import com.cardapio.cardapio_api.dto.ErroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rotas exclusivas do administrador (exigem token JWT com perfil ADMIN).
 * Nos próximos passos, o CRUD do cardápio e a gestão de pedidos ficam aqui.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administração", description = "Rotas protegidas: exigem login de administrador")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @GetMapping("/me")
    @Operation(
            summary = "Dados do administrador logado",
            description = "Devolve e-mail, nome e perfis do usuário dono do token. "
                    + "Serve para o frontend conferir se a sessão ainda é válida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão de administrador",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public UsuarioLogadoResponse me(@AuthenticationPrincipal Jwt jwt) {
        List<String> perfis = jwt.getClaimAsStringList("roles");
        return new UsuarioLogadoResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("nome"),
                perfis == null ? List.of() : perfis);
    }
}
