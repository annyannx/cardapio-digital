package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.AdminDtos.ItemAdminDto;
import com.cardapio.cardapio_api.dto.AdminDtos.ItemAdminRequest;
import com.cardapio.cardapio_api.dto.ErroResponse;
import com.cardapio.cardapio_api.service.AdminItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD de itens do cardápio pelo administrador (RF006).
 * Todas as rotas exigem login de administrador (token JWT).
 */
@RestController
@RequestMapping("/api/admin/itens")
@Tag(name = "Administração - Cardápio", description = "Cadastro, edição e remoção de itens (exige login de administrador)")
@SecurityRequirement(name = "bearerAuth")
public class AdminItemController {

    private final AdminItemService adminItemService;

    public AdminItemController(AdminItemService adminItemService) {
        this.adminItemService = adminItemService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os itens",
            description = "Inclui os itens indisponíveis. Use categoriaId para filtrar por categoria.")
    public List<ItemAdminDto> listar(
            @RequestParam(value = "categoriaId", required = false) Long categoriaId) {
        return adminItemService.listar(categoriaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um item pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ItemAdminDto buscar(@PathVariable("id") Long id) {
        return adminItemService.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um item",
            description = "Cria o item com seus tamanhos e preços. Nome, categoria e ao menos um tamanho são obrigatórios.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<ItemAdminDto> criar(@Valid @RequestBody ItemAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminItemService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita um item",
            description = "Substitui os dados do item. A lista de tamanhos enviada passa a ser a lista completa: "
                    + "tamanhos que não vierem são removidos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ItemAdminDto atualizar(@PathVariable("id") Long id, @Valid @RequestBody ItemAdminRequest request) {
        return adminItemService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um item permanentemente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removido"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        adminItemService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
