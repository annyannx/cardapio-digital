package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.ErroResponse;
import com.cardapio.cardapio_api.dto.PedidoDtos.PedidoRequest;
import com.cardapio.cardapio_api.dto.PedidoDtos.PedidoResponse;
import com.cardapio.cardapio_api.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público para o cliente enviar um pedido (RF004).
 */
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Envio de pedidos pelo cliente")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(
            summary = "Cria um novo pedido",
            description = "Recebe o nome do cliente, a mesa e os itens (id do tamanho, quantidade e "
                    + "opcionais). Os preços são buscados no banco e o total é calculado pelo servidor. "
                    + "Devolve o número do pedido e o status inicial (RECEBIDO).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "400",
                    description = "Dados inválidos (campo vazio, item indisponível, opcional inválido...)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse pedido = pedidoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }
}
