package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.ErroResponse;
import com.cardapio.cardapio_api.dto.PedidoAdminDtos.AcompanhamentoDto;
import com.cardapio.cardapio_api.service.AcompanhamentoService;
import com.cardapio.cardapio_api.service.PedidoStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Rotas públicas para o cliente acompanhar o seu pedido pelo número
 * e confirmar que o recebeu. Mostram só o status e as etapas (nenhum dado pessoal).
 */
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Envio e acompanhamento de pedidos pelo cliente")
public class AcompanhamentoController {

    private final AcompanhamentoService acompanhamentoService;
    private final PedidoStreamService pedidoStreamService;

    public AcompanhamentoController(AcompanhamentoService acompanhamentoService,
                                    PedidoStreamService pedidoStreamService) {
        this.acompanhamentoService = acompanhamentoService;
        this.pedidoStreamService = pedidoStreamService;
    }

    @GetMapping("/{numero}")
    @Operation(summary = "Consulta a situação de um pedido",
            description = "Devolve o status atual e o histórico de etapas do pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public AcompanhamentoDto consultar(@PathVariable("numero") Long numero) {
        return acompanhamentoService.buscar(numero);
    }

    @PostMapping("/{numero}/confirmar-recebimento")
    @Operation(summary = "Confirma que o cliente recebeu o pedido",
            description = "Só é possível depois que o pedido foi marcado como ENTREGUE. "
                    + "O painel do administrador é avisado em tempo real. "
                    + "Confirmar mais de uma vez não causa erro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recebimento confirmado"),
            @ApiResponse(responseCode = "400", description = "O pedido ainda não foi entregue",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public AcompanhamentoDto confirmarRecebimento(@PathVariable("numero") Long numero) {
        return acompanhamentoService.confirmarRecebimento(numero);
    }

    @GetMapping(value = "/{numero}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Tempo real: acompanha o pedido (SSE)",
            description = "Mantém a conexão aberta e envia o evento 'status' sempre que o status do pedido mudar "
                    + "ou o recebimento for confirmado. "
                    + "Não use o 'Try it out' do Swagger para esta rota: ela não termina.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conexão aberta"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public SseEmitter acompanhar(@PathVariable("numero") Long numero) {
        // Se o pedido não existir, a consulta abaixo já responde 404 e a conexão nem abre.
        AcompanhamentoDto situacaoAtual = acompanhamentoService.buscar(numero);
        return pedidoStreamService.abrirParaCliente(numero, situacaoAtual);
    }
}
