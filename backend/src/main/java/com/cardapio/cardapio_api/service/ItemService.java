package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.CardapioDtos.AdicionalDto;
import com.cardapio.cardapio_api.dto.CardapioDtos.TamanhoDto;
import com.cardapio.cardapio_api.dto.ItemDetalheDto;
import com.cardapio.cardapio_api.model.Adicional;
import com.cardapio.cardapio_api.model.Categoria;
import com.cardapio.cardapio_api.model.Item;
import com.cardapio.cardapio_api.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Regras de negócio da consulta de detalhes de um item (RF002).
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public ItemDetalheDto buscarDetalhe(Long id) {
        // Itens indisponíveis (ou de categorias desativadas) contam como "não encontrados".
        Item item = itemRepository.findById(id)
                .filter(i -> i.isDisponivel() && i.getCategoria().isAtiva())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item não encontrado: " + id));

        Categoria categoria = item.getCategoria();

        List<TamanhoDto> tamanhos = item.getTamanhos().stream()
                .map(t -> new TamanhoDto(t.getId(), t.getNome(), t.getPreco()))
                .toList();

        List<AdicionalDto> adicionais = categoria.getAdicionais().stream()
                .filter(Adicional::isAtivo)
                .map(a -> new AdicionalDto(a.getId(), a.getNome(), a.getPreco()))
                .toList();

        return new ItemDetalheDto(
                item.getId(),
                item.getCodigo(),
                item.getNome(),
                item.getDescricao(),
                item.getIngredientes(),
                item.getImagemUrl(),
                categoria.getId(),
                categoria.getNome(),
                tamanhos,
                adicionais);
    }
}
