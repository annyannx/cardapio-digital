package com.cardapio.cardapio_api.dto;

import com.cardapio.cardapio_api.dto.CardapioDtos.TamanhoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs do painel administrativo (RF006).
 */
public final class AdminDtos {

    private AdminDtos() {
    }

    // ------------------------------------------------------------------
    // ENTRADA
    // ------------------------------------------------------------------

    @Schema(description = "Tamanho/variação de um item, com preço")
    public record TamanhoRequest(
            @Schema(description = "Nome do tamanho", example = "Grande")
            @NotBlank(message = "Informe o nome do tamanho")
            @Size(max = 40, message = "O nome do tamanho pode ter no máximo 40 caracteres")
            String nome,

            @Schema(description = "Preço em reais", example = "69.90")
            @NotNull(message = "Informe o preço")
            @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
            @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
            BigDecimal preco) {
    }

    @Schema(description = "Dados de um item do cardápio (usado para cadastrar e para editar)")
    public record ItemAdminRequest(
            @Schema(description = "Id da categoria (veja GET /api/admin/categorias)", example = "1")
            @NotNull(message = "Informe a categoria")
            Long categoriaId,

            @Schema(description = "Código do item no cardápio (opcional)", example = "221")
            @Size(max = 10, message = "O código pode ter no máximo 10 caracteres")
            String codigo,

            @Schema(description = "Nome do item", example = "Pizza Teste")
            @NotBlank(message = "Informe o nome do item")
            @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres")
            String nome,

            @Schema(description = "Descrição resumida (opcional)", example = "Mussarela, tomate e manjericão.")
            @Size(max = 255, message = "A descrição pode ter no máximo 255 caracteres")
            String descricao,

            @Schema(description = "Ingredientes (opcional)", example = "Mussarela, tomate, manjericão e azeitona.")
            @Size(max = 2000, message = "Os ingredientes podem ter no máximo 2000 caracteres")
            String ingredientes,

            @Schema(description = "URL da imagem (use a URL devolvida por POST /api/admin/imagens)")
            @Size(max = 500, message = "A URL da imagem pode ter no máximo 500 caracteres")
            String imagemUrl,

            @Schema(description = "Se o item aparece no cardápio dos clientes (padrão: true)", example = "true")
            Boolean disponivel,

            @Schema(description = "Tamanhos com preço (pelo menos um)")
            @NotEmpty(message = "Informe pelo menos um tamanho com preço")
            @Valid
            List<TamanhoRequest> tamanhos) {
    }

    // ------------------------------------------------------------------
    // SAÍDA
    // ------------------------------------------------------------------

    @Schema(description = "Item do cardápio, como o administrador o enxerga")
    public record ItemAdminDto(
            Long id,
            Long categoriaId,
            String categoriaNome,
            String codigo,
            String nome,
            String descricao,
            String ingredientes,
            String imagemUrl,
            boolean disponivel,
            List<TamanhoDto> tamanhos) {
    }

    @Schema(description = "Categoria do cardápio")
    public record CategoriaAdminDto(
            Long id,
            String nome,
            int ordem,
            boolean ativa) {
    }

    @Schema(description = "Resultado do upload de uma imagem")
    public record ImagemResponse(
            @Schema(description = "URL pública da imagem enviada") String url) {
    }
}
