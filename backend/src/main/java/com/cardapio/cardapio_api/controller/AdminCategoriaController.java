package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.AdminDtos.CategoriaAdminDto;
import com.cardapio.cardapio_api.service.AdminCategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lista as categorias para o painel do administrador.
 */
@RestController
@RequestMapping("/api/admin/categorias")
@Tag(name = "Administração - Cardápio", description = "Cadastro, edição e remoção de itens (exige login de administrador)")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoriaController {

    private final AdminCategoriaService adminCategoriaService;

    public AdminCategoriaController(AdminCategoriaService adminCategoriaService) {
        this.adminCategoriaService = adminCategoriaService;
    }

    @GetMapping
    @Operation(summary = "Lista as categorias",
            description = "Use o id da categoria ao cadastrar ou editar um item.")
    public List<CategoriaAdminDto> listar() {
        return adminCategoriaService.listar();
    }
}
