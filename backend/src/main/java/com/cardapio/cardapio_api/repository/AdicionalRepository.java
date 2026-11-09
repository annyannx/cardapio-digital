package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.Adicional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso ao banco para os opcionais (adicionais).
 */
public interface AdicionalRepository extends JpaRepository<Adicional, Long> {
}
