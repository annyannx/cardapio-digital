package com.cardapio.cardapio_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Formato padrão de todas as respostas de erro da API.
 */
@Schema(description = "Resposta de erro padrão da API")
public record ErroResponse(
        @Schema(description = "Código HTTP do erro", example = "400") int status,
        @Schema(description = "Mensagem principal", example = "Dados inválidos") String mensagem,
        @Schema(description = "Detalhes (por exemplo, os campos com problema)") List<String> detalhes) {
}
