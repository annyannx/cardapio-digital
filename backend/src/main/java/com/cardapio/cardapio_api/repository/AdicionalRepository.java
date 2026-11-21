package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.Adicional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso ao banco para os opcionais (adicionais).
 */
public interface AdicionalRepository extends JpaRepository<Adicional, Long> {

    // Todos os opcionais, em ordem alfabética (painel do administrador).
    List<Adicional> findAllByOrderByNomeAsc();

    // Já existe um opcional com este nome? (sem diferenciar maiúsculas de minúsculas)
    boolean existsByNomeIgnoreCase(String nome);

    // Idem, ignorando o próprio opcional (usado na edição).
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
