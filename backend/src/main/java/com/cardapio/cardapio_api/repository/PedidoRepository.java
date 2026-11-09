package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso ao banco para pedidos.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
