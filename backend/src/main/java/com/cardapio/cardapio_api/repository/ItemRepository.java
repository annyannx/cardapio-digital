package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso ao banco para itens do cardápio.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
}
