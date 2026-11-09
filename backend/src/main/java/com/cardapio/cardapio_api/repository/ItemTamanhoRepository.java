package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.ItemTamanho;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso ao banco para os tamanhos (e preços) dos itens.
 */
public interface ItemTamanhoRepository extends JpaRepository<ItemTamanho, Long> {
}
