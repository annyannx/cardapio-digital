package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.PedidoAdminDtos.AcompanhamentoDto;
import com.cardapio.cardapio_api.dto.PedidoAdminDtos.PedidoAdminDto;
import com.cardapio.cardapio_api.event.PedidoEventos;
import com.cardapio.cardapio_api.model.Pedido;
import com.cardapio.cardapio_api.model.StatusPedido;
import com.cardapio.cardapio_api.repository.PedidoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Regras da gestão de pedidos pelo administrador:
 * listar (RF007) e atualizar o status (RF008).
 */
@Service
public class AdminPedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final ApplicationEventPublisher eventos;

    public AdminPedidoService(PedidoRepository pedidoRepository,
                              PedidoMapper pedidoMapper,
                              ApplicationEventPublisher eventos) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.eventos = eventos;
    }

    /** Lista os pedidos mais recentes. Se status vier preenchido, filtra por ele. */
    @Transactional(readOnly = true)
    public List<PedidoAdminDto> listar(StatusPedido status) {
        List<Pedido> pedidos = (status == null)
                ? pedidoRepository.findTop200ByOrderByCriadoEmDesc()
                : pedidoRepository.findTop200ByStatusOrderByCriadoEmDesc(status);
        return pedidos.stream().map(pedidoMapper::paraAdminDto).toList();
    }

    @Transactional(readOnly = true)
    public PedidoAdminDto buscar(Long numero) {
        return pedidoMapper.paraAdminDto(buscarPedido(numero));
    }

    /**
     * Muda o status do pedido, grava no histórico quem mudou e quando,
     * e avisa em tempo real o painel do administrador e o cliente.
     */
    @Transactional
    public PedidoAdminDto alterarStatus(Long numero, StatusPedido novoStatus, Long usuarioId) {
        Pedido pedido = buscarPedido(numero);

        if (pedido.getStatus() == novoStatus) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O pedido " + numero + " já está com o status " + novoStatus.name());
        }

        pedido.alterarStatus(novoStatus, usuarioId);

        PedidoAdminDto paraAdmin = pedidoMapper.paraAdminDto(pedido);
        AcompanhamentoDto paraCliente = pedidoMapper.paraAcompanhamento(pedido);
        eventos.publishEvent(new PedidoEventos.StatusAlterado(paraAdmin, paraCliente));

        return paraAdmin;
    }

    private Pedido buscarPedido(Long numero) {
        return pedidoRepository.findById(numero)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido não encontrado: " + numero));
    }
}
