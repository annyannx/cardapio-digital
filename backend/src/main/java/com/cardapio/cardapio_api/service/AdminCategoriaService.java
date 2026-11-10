package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.AdminDtos.CategoriaAdminDto;
import com.cardapio.cardapio_api.repository.CategoriaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Consulta de categorias para o painel do administrador
 * (o admin precisa do id da categoria ao cadastrar um item).
 */
@Service
public class AdminCategoriaService {

    private final CategoriaRepository categoriaRepository;

    public AdminCategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaAdminDto> listar() {
        return categoriaRepository.findAll(Sort.by("ordem")).stream()
                .map(c -> new CategoriaAdminDto(c.getId(), c.getNome(), c.getOrdem(), c.isAtiva()))
                .toList();
    }
}
