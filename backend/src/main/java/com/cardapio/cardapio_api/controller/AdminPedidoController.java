package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.ErroResponse;
import com.cardapio.cardapio_api.dto.PedidoAdminDtos.AlterarStatusRequest;
import com.cardapio.cardapio_api.dto.PedidoAdminDtos.PedidoAdminDto;
import com.cardapio.cardapio_api.model.StatusPedido;
import com.cardapio.cardapio_api.service.AdminPedidoService;
import com.cardapio.cardapio_api.service.PedidoStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Gestão de pedidos pelo administrador (RF007 e RF008).
 * Todas as rotas exigem login de administrador (token JWT).
 */
@RestController
@RequestMapping("/api/admin/pedidos")
@Tag(name = "Administração - Pedidos", description = "Lista de pedidos, mudança de status e tempo real (exige login de administrador)")
@SecurityRequirement(name = "bearerAuth")
public class AdminPedidoController {

    private final AdminPedidoService adminPedidoService;
    private final PedidoStreamService pedidoStreamService;

    public AdminPedidoController(AdminPedidoService adminPedidoService,
                                 PedidoStreamService pedidoStreamService) {
        this.adminPedidoService = adminPedidoService;
        this.pedidoStreamService = pedidoStreamService;
    }

    @GetMapping
    @Operation(summary = "Lista os pedidos",
            description = "Devolve os 200 pedidos mais recentes, com itens, valor total, status e histórico. "
                    + "Use o parâmetro status (RECEBIDO, EM_PREPARO, PRONTO ou ENTREGUE) para filtrar.")
    public List<PedidoAdminDto> listar(
            @RequestParam(value = "status", required = false) StatusPedido status) {
        return adminPedidoService.listar(status);
    }

    @GetMapping("/{numero}")
    @Operation(summary = "Busca um pedido pelo número")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public PedidoAdminDto buscar(@PathVariable("numero") Long numero) {
        return adminPedidoService.buscar(numero);
    }

    @PatchMapping("/{numero}/status")
    @Operation(summary = "Atualiza o status de um pedido",
            description = "Grava a mudança no histórico (quem mudou e quando) e avisa em tempo real "
                    + "o painel do administrador e o cliente que acompanha o pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status inválido ou igual ao atual",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public PedidoAdminDto alterarStatus(@PathVariable("numero") Long numero,
                                        @Valid @RequestBody AlterarStatusRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        // O id de quem está mudando o status vem de dentro do token JWT (campo "uid").
        Number uid = jwt.getClaim("uid");
        Long usuarioId = (uid == null) ? null : uid.longValue();
        return adminPedidoService.alterarStatus(numero, request.status(), usuarioId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Tempo real: novos pedidos e mudanças de status (SSE)",
            description = "Mantém a conexão aberta e envia os eventos 'pedido-novo' e 'pedido-atualizado' "
                    + "assim que acontecem. Não use o 'Try it out' do Swagger para esta rota: ela não termina.")
    public SseEmitter acompanhar() {
        return pedidoStreamService.abrirParaAdministrador();
    }
}
