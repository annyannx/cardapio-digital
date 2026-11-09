package com.cardapio.cardapio_api.dto;

import com.cardapio.cardapio_api.dto.CardapioDtos.AdicionalDto;
import com.cardapio.cardapio_api.dto.CardapioDtos.TamanhoDto;

import java.util.List;

/**
 * Detalhes completos de um item do cardápio (RF002): informações do item,
 * tamanhos com preço e os opcionais que podem ser acrescentados.
 */
public record ItemDetalheDto(
        Long id,
        String codigo,
        String nome,
        String descricao,
        String ingredientes,
        String imagemUrl,
        Long categoriaId,
        String categoriaNome,
        List<TamanhoDto> tamanhos,
        List<AdicionalDto> adicionais) {
}
