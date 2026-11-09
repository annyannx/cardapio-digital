package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.ErroResponse;
import com.cardapio.cardapio_api.dto.ItemDetalheDto;
import com.cardapio.cardapio_api.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público com os detalhes de um item do cardápio (RF002).
 */
@RestController
@RequestMapping("/api/cardapio/itens")
@Tag(name = "Cardápio", description = "Consulta do cardápio pelo cliente")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Detalhes de um item",
            description = "Devolve nome, descrição, ingredientes, imagem, tamanhos com preço "
                    + "e os opcionais disponíveis para o item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou indisponível",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ItemDetalheDto buscar(@PathVariable("id") Long id) {
        return itemService.buscarDetalhe(id);
    }
}
