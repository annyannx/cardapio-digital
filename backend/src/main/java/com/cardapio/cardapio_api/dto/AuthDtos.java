package com.cardapio.cardapio_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTOs da autenticação (RF005).
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    @Schema(description = "Credenciais do administrador")
    public record LoginRequest(
            @Schema(description = "E-mail do administrador", example = "admin@cardapio.com")
            @NotBlank(message = "Informe o e-mail")
            @Size(max = 150, message = "O e-mail pode ter no máximo 150 caracteres")
            String email,

            @Schema(description = "Senha do administrador", example = "admin123")
            @NotBlank(message = "Informe a senha")
            @Size(max = 100, message = "A senha pode ter no máximo 100 caracteres")
            String senha) {
    }

    @Schema(description = "Resultado do login")
    public record LoginResponse(
            @Schema(description = "Token JWT: envie no cabeçalho Authorization como 'Bearer <token>'")
            String token,
            @Schema(description = "Tipo do token", example = "Bearer")
            String tipo,
            @Schema(description = "Tempo de validade do token, em segundos", example = "28800")
            long expiraEmSegundos,
            @Schema(description = "Nome do usuário", example = "Administrador")
            String nome,
            @Schema(description = "Perfil do usuário", example = "ADMIN")
            String perfil) {
    }

    @Schema(description = "Dados do usuário autenticado")
    public record UsuarioLogadoResponse(
            String email,
            String nome,
            List<String> perfis) {
    }
}
