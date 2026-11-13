package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.PedidoAdminDtos.AcompanhamentoDto;
import com.cardapio.cardapio_api.event.PedidoEventos;
import com.cardapio.cardapio_api.model.Pedido;
import com.cardapio.cardapio_api.model.StatusPedido;
import com.cardapio.cardapio_api.repository.PedidoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Permite ao cliente consultar a situação do seu pedido pelo número e
 * confirmar que recebeu. Devolve só o status e o histórico de etapas
 * (sem nome do cliente nem itens).
 */
@Service
public class AcompanhamentoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final ApplicationEventPublisher eventos;

    public AcompanhamentoService(PedidoRepository pedidoRepository,
                                 PedidoMapper pedidoMapper,
                                 ApplicationEventPublisher eventos) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public AcompanhamentoDto buscar(Long numero) {
        return pedidoRepository.findById(numero)
                .map(pedidoMapper::paraAcompanhamento)
                .orElseThrow(() -> naoEncontrado(numero));
    }

    /**
     * O cliente confirma que recebeu o pedido (feedback do cliente para o administrador).
     * Só é possível depois que o pedido foi marcado como ENTREGUE.
     * Confirmar de novo não causa erro nem muda nada.
     * Ao confirmar, o painel do administrador e a tela do cliente são avisados em tempo real.
     */
    @Transactional
    public AcompanhamentoDto confirmarRecebimento(Long numero) {
        Pedido pedido = pedidoRepository.findById(numero)
                .orElseThrow(() -> naoEncontrado(numero));

        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O pedido ainda não foi entregue. Você poderá confirmar o recebimento depois da entrega.");
        }

        boolean jaConfirmado = pedido.getConfirmadoEm() != null;
        pedido.confirmarRecebimento();

        AcompanhamentoDto paraCliente = pedidoMapper.paraAcompanhamento(pedido);
        if (!jaConfirmado) {
            eventos.publishEvent(new PedidoEventos.StatusAlterado(
                    pedidoMapper.paraAdminDto(pedido), paraCliente));
        }
        return paraCliente;
    }

    private ResponseStatusException naoEncontrado(Long numero) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado: " + numero);
    }
}
