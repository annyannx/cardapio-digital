package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso ao banco para itens do cardápio.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Todos os itens (inclusive indisponíveis), na ordem das categorias.
    List<Item> findAllByOrderByCategoriaOrdemAscCodigoAscNomeAsc();

    // Itens de uma categoria.
    List<Item> findByCategoriaIdOrderByCodigoAscNomeAsc(Long categoriaId);
}
