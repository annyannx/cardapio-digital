package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.AdicionalAdminDtos.AdicionalAdminDto;
import com.cardapio.cardapio_api.dto.AdicionalAdminDtos.AdicionalAdminRequest;
import com.cardapio.cardapio_api.model.Adicional;
import com.cardapio.cardapio_api.model.Categoria;
import com.cardapio.cardapio_api.repository.AdicionalRepository;
import com.cardapio.cardapio_api.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Gerenciamento dos opcionais (adicionais) pelo administrador:
 * listar, cadastrar, editar (nome, preço, ativo e categorias) e remover.
 */
@Service
public class AdminAdicionalService {

    private final AdicionalRepository adicionalRepository;
    private final CategoriaRepository categoriaRepository;

    public AdminAdicionalService(AdicionalRepository adicionalRepository,
                                 CategoriaRepository categoriaRepository) {
        this.adicionalRepository = adicionalRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<AdicionalAdminDto> listar() {
        Map<Long, List<Long>> categoriasPorAdicional = new HashMap<>();
        for (Categoria categoria : categoriaRepository.findAll()) {
            for (Adicional adicional : categoria.getAdicionais()) {
                categoriasPorAdicional
                        .computeIfAbsent(adicional.getId(), k -> new ArrayList<>())
                        .add(categoria.getId());
            }
        }

        return adicionalRepository.findAllByOrderByNomeAsc().stream()
                .map(a -> paraDto(a, categoriasPorAdicional.getOrDefault(a.getId(), List.of())))
                .toList();
    }

    @Transactional
    public AdicionalAdminDto criar(AdicionalAdminRequest request) {
        String nome = request.nome().trim();
        if (adicionalRepository.existsByNomeIgnoreCase(nome)) {
            throw erro("Já existe um opcional chamado \"" + nome + "\"");
        }
        List<Categoria> categorias = buscarCategorias(request.categoriaIds());

        Adicional adicional = adicionalRepository.save(
                new Adicional(nome, request.preco(), request.ativo() == null || request.ativo()));

        for (Categoria categoria : categorias) {
            categoria.getAdicionais().add(adicional);
        }
        return paraDto(adicional, idsDe(categorias));
    }

    @Transactional
    public AdicionalAdminDto atualizar(Long id, AdicionalAdminRequest request) {
        Adicional adicional = buscar(id);

        String nome = request.nome().trim();
        if (adicionalRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw erro("Já existe outro opcional chamado \"" + nome + "\"");
        }
        List<Categoria> desejadas = buscarCategorias(request.categoriaIds());
        Set<Long> idsDesejados = new TreeSet<>(idsDe(desejadas));

        adicional.atualizar(nome, request.preco(), request.ativo() == null || request.ativo());

        // Deixa as categorias do opcional iguais às marcadas.
        for (Categoria categoria : categoriaRepository.findAll()) {
            boolean tem = categoria.getAdicionais().stream().anyMatch(a -> Objects.equals(a.getId(), id));
            boolean deve = idsDesejados.contains(categoria.getId());
            if (deve && !tem) {
                categoria.getAdicionais().add(adicional);
            } else if (!deve && tem) {
                categoria.getAdicionais().removeIf(a -> Objects.equals(a.getId(), id));
            }
        }
        return paraDto(adicional, new ArrayList<>(idsDesejados));
    }

    /** Remove o opcional. Pedidos antigos não são afetados (guardam nome e preço). */
    @Transactional
    public void remover(Long id) {
        Adicional adicional = buscar(id);
        for (Categoria categoria : categoriaRepository.findAll()) {
            categoria.getAdicionais().removeIf(a -> Objects.equals(a.getId(), id));
        }
        adicionalRepository.delete(adicional);
    }

    // ------------------------------------------------------------------

    private Adicional buscar(Long id) {
        return adicionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Opcional não encontrado: " + id));
    }

    private List<Categoria> buscarCategorias(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> unicos = ids.stream().filter(Objects::nonNull).distinct().toList();
        List<Categoria> categorias = categoriaRepository.findAllById(unicos);
        if (categorias.size() != unicos.size()) {
            throw erro("Uma ou mais categorias não foram encontradas");
        }
        return categorias;
    }

    private List<Long> idsDe(List<Categoria> categorias) {
        return categorias.stream().map(Categoria::getId).sorted().toList();
    }

    private AdicionalAdminDto paraDto(Adicional adicional, List<Long> categoriaIds) {
        List<Long> ordenados = categoriaIds.stream().sorted().toList();
        return new AdicionalAdminDto(
                adicional.getId(), adicional.getNome(), adicional.getPreco(), adicional.isAtivo(), ordenados);
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
