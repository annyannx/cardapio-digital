package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.CardapioDtos.CategoriaDto;
import com.cardapio.cardapio_api.service.CardapioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints públicos do cardápio (não exigem login).
 */
@RestController
@RequestMapping("/api/cardapio")
@Tag(name = "Cardápio", description = "Consulta do cardápio pelo cliente")
public class CardapioController {

    private final CardapioService cardapioService;

    public CardapioController(CardapioService cardapioService) {
        this.cardapioService = cardapioService;
    }

    @GetMapping
    @Operation(
            summary = "Lista o cardápio completo",
            description = "Devolve as categorias ativas, cada uma com seus itens disponíveis, "
                    + "tamanhos com preço e opcionais.")
    public List<CategoriaDto> listarCardapio() {
        return cardapioService.listarCardapio();
    }
}
