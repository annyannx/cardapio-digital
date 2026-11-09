package com.cardapio.cardapio_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs dos pedidos: o que o cliente envia (Request) e o que a API devolve (Response).
 * Os preços NUNCA vêm do cliente: o servidor busca no banco e calcula o total.
 */
public final class PedidoDtos {

    private PedidoDtos() {
    }

    // ------------------------------------------------------------------
    // ENTRADA (o que o cliente envia)
    // ------------------------------------------------------------------

    @Schema(description = "Dados para criar um pedido")
    public record PedidoRequest(
            @Schema(description = "Nome do cliente", example = "Maria")
            @NotBlank(message = "Informe o nome do cliente")
            @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres")
            String clienteNome,

            @Schema(description = "Número ou identificação da mesa (opcional)", example = "5")
            @Size(max = 20, message = "A mesa pode ter no máximo 20 caracteres")
            String mesa,

            @Schema(description = "Observação geral do pedido (opcional)", example = "Sem cebola")
            @Size(max = 255, message = "A observação pode ter no máximo 255 caracteres")
            String observacao,

            @Schema(description = "Itens do pedido (pelo menos um)")
            @NotEmpty(message = "O pedido precisa ter pelo menos um item")
            @Valid
            List<ItemPedidoRequest> itens) {
    }

    @Schema(description = "Um item do pedido")
    public record ItemPedidoRequest(
            @Schema(description = "Id do tamanho/variação escolhido (vem do cardápio)", example = "1")
            @NotNull(message = "Informe o tamanho do item")
            Long tamanhoId,

            @Schema(description = "Quantidade (1 a 50)", example = "2")
            @NotNull(message = "Informe a quantidade")
            @Min(value = 1, message = "A quantidade mínima é 1")
            @Max(value = 50, message = "A quantidade máxima é 50")
            Integer quantidade,

            @Schema(description = "Observação do item (opcional)", example = "Bem passada")
            @Size(max = 255, message = "A observação do item pode ter no máximo 255 caracteres")
            String observacao,

            @Schema(description = "Ids dos opcionais escolhidos (opcional)")
            List<Long> adicionaisIds) {
    }

    // ------------------------------------------------------------------
    // SAÍDA (o que a API devolve)
    // ------------------------------------------------------------------

    @Schema(description = "Pedido registrado")
    public record PedidoResponse(
            @Schema(description = "Número do pedido") Long numero,
            @Schema(description = "Status atual", example = "RECEBIDO") String status,
            @Schema(description = "Valor total calculado pelo servidor") BigDecimal valorTotal,
            String clienteNome,
            String mesa,
            List<ItemPedidoResponse> itens) {
    }

    public record ItemPedidoResponse(
            String nome,
            String tamanho,
            @Schema(description = "Preço unitário do tamanho, sem os opcionais") BigDecimal precoUnitario,
            int quantidade,
            @Schema(description = "(preço unitário + opcionais) x quantidade") BigDecimal subtotal,
            String observacao,
            List<AdicionalPedidoResponse> adicionais) {
    }

    public record AdicionalPedidoResponse(String nome, BigDecimal preco) {
    }
}
