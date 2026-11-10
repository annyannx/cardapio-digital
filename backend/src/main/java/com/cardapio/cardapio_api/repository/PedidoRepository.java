package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.Pedido;
import com.cardapio.cardapio_api.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso ao banco para pedidos.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Os 200 pedidos mais recentes.
    List<Pedido> findTop200ByOrderByCriadoEmDesc();

    // Os 200 pedidos mais recentes de um status (filtro do painel).
    List<Pedido> findTop200ByStatusOrderByCriadoEmDesc(StatusPedido status);
}
