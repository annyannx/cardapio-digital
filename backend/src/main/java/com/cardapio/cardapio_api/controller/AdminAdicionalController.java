package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.AdicionalAdminDtos.AdicionalAdminDto;
import com.cardapio.cardapio_api.dto.AdicionalAdminDtos.AdicionalAdminRequest;
import com.cardapio.cardapio_api.dto.ErroResponse;
import com.cardapio.cardapio_api.service.AdminAdicionalService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gerenciamento dos opcionais (adicionais) pelo administrador.
 * Todas as rotas exigem login de administrador (token JWT).
 */
@RestController
@RequestMapping("/api/admin/adicionais")
@Tag(name = "Administração - Cardápio", description = "Cadastro, edição e remoção de itens (exige login de administrador)")
@SecurityRequirement(name = "bearerAuth")
public class AdminAdicionalController {

    private final AdminAdicionalService adminAdicionalService;

    public AdminAdicionalController(AdminAdicionalService adminAdicionalService) {
        this.adminAdicionalService = adminAdicionalService;
    }

    @GetMapping
    @Operation(summary = "Lista os opcionais",
            description = "Todos os opcionais (inclusive os inativos), com as categorias em que estão disponíveis.")
    public List<AdicionalAdminDto> listar() {
        return adminAdicionalService.listar();
    }

    @PostMapping
    @Operation(summary = "Cadastra um opcional")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Opcional cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome repetido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<AdicionalAdminDto> criar(@Valid @RequestBody AdicionalAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminAdicionalService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita um opcional",
            description = "Altera nome, preço, se está ativo e as categorias em que aparece.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Opcional atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome repetido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Opcional não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public AdicionalAdminDto atualizar(@PathVariable("id") Long id,
                                       @Valid @RequestBody AdicionalAdminRequest request) {
        return adminAdicionalService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um opcional",
            description = "Remoção permanente. Pedidos antigos não são afetados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Opcional removido"),
            @ApiResponse(responseCode = "404", description = "Opcional não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        adminAdicionalService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
