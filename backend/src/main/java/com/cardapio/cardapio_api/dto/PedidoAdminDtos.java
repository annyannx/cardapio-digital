package com.cardapio.cardapio_api.dto;

import com.cardapio.cardapio_api.dto.PedidoDtos.ItemPedidoResponse;
import com.cardapio.cardapio_api.model.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs da gestão de pedidos (RF007 e RF008) e do acompanhamento pelo cliente.
 * As datas são texto no formato ISO com fuso de Brasília, ex.: 2026-11-09T18:35:00-03:00.
 */
public final class PedidoAdminDtos {

    private PedidoAdminDtos() {
    }

    // ------------------------------------------------------------------
    // ENTRADA
    // ------------------------------------------------------------------

    @Schema(description = "Novo status do pedido")
    public record AlterarStatusRequest(
            @Schema(description = "Novo status", example = "EM_PREPARO")
            @NotNull(message = "Informe o novo status")
            StatusPedido status) {
    }

    // ------------------------------------------------------------------
    // SAÍDA (administrador)
    // ------------------------------------------------------------------

    @Schema(description = "Uma mudança de status no histórico do pedido")
    public record HistoricoDto(
            @Schema(description = "Status anterior (vazio na criação do pedido)") String statusAnterior,
            String statusNovo,
            @Schema(description = "Quem mudou ('Cliente' na criação do pedido)") String usuario,
            @Schema(description = "Data e hora da mudança") String dataHora) {
    }

    @Schema(description = "Pedido completo, como o administrador o enxerga")
    public record PedidoAdminDto(
            @Schema(description = "Número do pedido") Long numero,
            @Schema(description = "RECEBIDO, EM_PREPARO, PRONTO ou ENTREGUE") String status,
            String clienteNome,
            String mesa,
            String observacao,
            BigDecimal valorTotal,
            String criadoEm,
            String atualizadoEm,
            List<ItemPedidoResponse> itens,
            List<HistoricoDto> historico) {
    }

    // ------------------------------------------------------------------
    // SAÍDA (cliente acompanhando o pedido)
    // ------------------------------------------------------------------

    @Schema(description = "Uma etapa do pedido, para o cliente")
    public record HistoricoPublicoDto(String status, String dataHora) {
    }

    @Schema(description = "Situação do pedido para o cliente (sem dados pessoais)")
    public record AcompanhamentoDto(
            Long numero,
            String status,
            BigDecimal valorTotal,
            String criadoEm,
            String atualizadoEm,
            List<HistoricoPublicoDto> historico) {
    }
}
