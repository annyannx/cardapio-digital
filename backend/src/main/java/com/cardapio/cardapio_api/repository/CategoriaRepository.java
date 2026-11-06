package com.cardapio.cardapio_api.repository;

import com.cardapio.cardapio_api.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso ao banco para categorias.
 * O Spring cria a implementação sozinho a partir do nome do método.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Busca só as categorias ativas, na ordem definida na coluna "ordem".
    List<Categoria> findByAtivaTrueOrderByOrdemAsc();
}
