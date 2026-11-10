package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.PedidoAdminDtos.AcompanhamentoDto;
import com.cardapio.cardapio_api.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Permite ao cliente consultar a situação do seu pedido pelo número.
 * Devolve só o status e o histórico de etapas (sem nome do cliente nem itens).
 */
@Service
public class AcompanhamentoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;

    public AcompanhamentoService(PedidoRepository pedidoRepository, PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
    }

    @Transactional(readOnly = true)
    public AcompanhamentoDto buscar(Long numero) {
        return pedidoRepository.findById(numero)
                .map(pedidoMapper::paraAcompanhamento)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido não encontrado: " + numero));
    }
}
