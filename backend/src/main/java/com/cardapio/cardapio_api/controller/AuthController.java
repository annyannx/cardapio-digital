package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.AuthDtos.LoginRequest;
import com.cardapio.cardapio_api.dto.AuthDtos.LoginResponse;
import com.cardapio.cardapio_api.dto.ErroResponse;
import com.cardapio.cardapio_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login do administrador (RF005). Rota pública: devolve o token JWT.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login do administrador")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login do administrador",
            description = "Confere e-mail e senha e devolve um token JWT. "
                    + "Use o token no botão Authorize do Swagger (ou no cabeçalho "
                    + "Authorization: Bearer <token>) para acessar as rotas de administração.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios não informados",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
