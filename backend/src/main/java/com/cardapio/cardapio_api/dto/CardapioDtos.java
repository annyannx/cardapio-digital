package com.cardapio.cardapio_api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs do cardápio: definem exatamente o que a API devolve em JSON.
 * Usamos DTOs (e não as entidades) para não expor o banco diretamente.
 */
public final class CardapioDtos {

    private CardapioDtos() {
    }

    /** Um tamanho com seu preço (ex.: Grande - 69.90). */
    public record TamanhoDto(Long id, String nome, BigDecimal preco) {
    }

    /** Um opcional (ex.: Catupiry Original - 6.00). */
    public record AdicionalDto(Long id, String nome, BigDecimal preco) {
    }

    /** Um item do cardápio, com seus tamanhos. */
    public record ItemDto(
            Long id,
            String codigo,
            String nome,
            String descricao,
            String ingredientes,
            String imagemUrl,
            List<TamanhoDto> tamanhos) {
    }

    /** Uma categoria, com seus itens e os opcionais disponíveis nela. */
    public record CategoriaDto(
            Long id,
            String nome,
            List<ItemDto> itens,
            List<AdicionalDto> adicionais) {
    }
}
