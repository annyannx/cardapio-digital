package com.cardapio.cardapio_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs do gerenciamento de opcionais (adicionais) pelo administrador.
 */
public final class AdicionalAdminDtos {

    private AdicionalAdminDtos() {
    }

    @Schema(description = "Dados de um opcional (usado para cadastrar e para editar)")
    public record AdicionalAdminRequest(
            @Schema(description = "Nome do opcional", example = "Catupiry Original")
            @NotBlank(message = "Informe o nome do opcional")
            @Size(max = 80, message = "O nome pode ter no máximo 80 caracteres")
            String nome,

            @Schema(description = "Preço em reais", example = "6.00")
            @NotNull(message = "Informe o preço")
            @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
            @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
            BigDecimal preco,

            @Schema(description = "Se o opcional aparece para os clientes (padrão: true)", example = "true")
            Boolean ativo,

            @Schema(description = "Ids das categorias em que o opcional pode ser escolhido "
                    + "(veja GET /api/admin/categorias). Ex.: Pizzas e Esfihas.")
            List<Long> categoriaIds) {
    }

    @Schema(description = "Opcional, como o administrador o enxerga")
    public record AdicionalAdminDto(
            Long id,
            String nome,
            BigDecimal preco,
            boolean ativo,
            @Schema(description = "Categorias em que o opcional está disponível") List<Long> categoriaIds) {
    }
}
