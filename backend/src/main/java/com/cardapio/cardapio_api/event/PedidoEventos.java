package com.cardapio.cardapio_api.event;

import com.cardapio.cardapio_api.dto.PedidoAdminDtos.AcompanhamentoDto;
import com.cardapio.cardapio_api.dto.PedidoAdminDtos.PedidoAdminDto;

/**
 * Eventos publicados quando algo acontece com um pedido.
 * O PedidoStreamService escuta esses eventos (depois que o banco confirma a gravação)
 * e avisa, em tempo real, o painel do administrador e o cliente.
 */
public final class PedidoEventos {

    private PedidoEventos() {
    }

    /** Um pedido novo foi registrado. */
    public record Criado(PedidoAdminDto pedido) {
    }

    /** O status de um pedido mudou. */
    public record StatusAlterado(PedidoAdminDto pedido, AcompanhamentoDto acompanhamento) {
    }
}
