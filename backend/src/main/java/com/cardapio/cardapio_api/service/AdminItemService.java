package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.AdminDtos.ItemAdminDto;
import com.cardapio.cardapio_api.dto.AdminDtos.ItemAdminRequest;
import com.cardapio.cardapio_api.dto.AdminDtos.TamanhoRequest;
import com.cardapio.cardapio_api.dto.CardapioDtos.TamanhoDto;
import com.cardapio.cardapio_api.model.Categoria;
import com.cardapio.cardapio_api.model.Item;
import com.cardapio.cardapio_api.model.ItemTamanho;
import com.cardapio.cardapio_api.repository.CategoriaRepository;
import com.cardapio.cardapio_api.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regras do CRUD de itens do cardápio pelo administrador (RF006):
 * listar, buscar, cadastrar, editar e remover.
 */
@Service
public class AdminItemService {

    private final ItemRepository itemRepository;
    private final CategoriaRepository categoriaRepository;

    public AdminItemService(ItemRepository itemRepository, CategoriaRepository categoriaRepository) {
        this.itemRepository = itemRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /** Lista todos os itens (inclusive os indisponíveis). Se categoriaId vier preenchido, filtra por ela. */
    @Transactional(readOnly = true)
    public List<ItemAdminDto> listar(Long categoriaId) {
        List<Item> itens = (categoriaId == null)
                ? itemRepository.findAllByOrderByCategoriaOrdemAscCodigoAscNomeAsc()
                : itemRepository.findByCategoriaIdOrderByCodigoAscNomeAsc(categoriaId);
        return itens.stream().map(this::paraDto).toList();
    }

    @Transactional(readOnly = true)
    public ItemAdminDto buscar(Long id) {
        return paraDto(buscarItem(id));
    }

    @Transactional
    public ItemAdminDto criar(ItemAdminRequest request) {
        Categoria categoria = buscarCategoria(request.categoriaId());
        validarTamanhos(request.tamanhos());

        Item item = new Item(
                categoria,
                limpar(request.codigo()),
                request.nome().trim(),
                limpar(request.descricao()),
                limpar(request.ingredientes()),
                limpar(request.imagemUrl()),
                request.disponivel() == null || request.disponivel());

        sincronizarTamanhos(item, request.tamanhos());
        return paraDto(itemRepository.save(item));
    }

    @Transactional
    public ItemAdminDto atualizar(Long id, ItemAdminRequest request) {
        Item item = buscarItem(id);
        Categoria categoria = buscarCategoria(request.categoriaId());
        validarTamanhos(request.tamanhos());

        item.atualizar(
                categoria,
                limpar(request.codigo()),
                request.nome().trim(),
                limpar(request.descricao()),
                limpar(request.ingredientes()),
                limpar(request.imagemUrl()),
                request.disponivel() == null || request.disponivel());

        sincronizarTamanhos(item, request.tamanhos());
        return paraDto(itemRepository.save(item));
    }

    /** Exclui o item permanentemente. Os pedidos antigos continuam intactos (guardam o nome e o preço). */
    @Transactional
    public void remover(Long id) {
        itemRepository.delete(buscarItem(id));
    }

    // ------------------------------------------------------------------

    /**
     * Deixa os tamanhos do item iguais aos enviados: atualiza preço/posição dos que já existem
     * (pelo nome), cria os novos e apaga os que sumiram da lista.
     */
    private void sincronizarTamanhos(Item item, List<TamanhoRequest> novos) {
        Map<String, ItemTamanho> existentes = new HashMap<>();
        for (ItemTamanho tamanho : item.getTamanhos()) {
            existentes.put(tamanho.getNome(), tamanho);
        }

        Set<String> nomesNovos = novos.stream()
                .map(t -> t.nome().trim())
                .collect(Collectors.toSet());

        item.getTamanhos().removeIf(t -> !nomesNovos.contains(t.getNome()));

        int ordem = 1;
        for (TamanhoRequest novo : novos) {
            String nome = novo.nome().trim();
            ItemTamanho existente = existentes.get(nome);
            if (existente != null) {
                existente.atualizar(novo.preco(), ordem);
            } else {
                item.adicionarTamanho(new ItemTamanho(nome, novo.preco(), ordem));
            }
            ordem++;
        }
    }

    private void validarTamanhos(List<TamanhoRequest> tamanhos) {
        Set<String> vistos = new HashSet<>();
        for (TamanhoRequest tamanho : tamanhos) {
            String nome = tamanho.nome().trim().toLowerCase();
            if (!vistos.add(nome)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tamanho repetido: " + tamanho.nome().trim());
            }
        }
    }

    private Item buscarItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item não encontrado: " + id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Categoria não encontrada: " + id));
    }

    private ItemAdminDto paraDto(Item item) {
        List<TamanhoDto> tamanhos = item.getTamanhos().stream()
                .sorted(Comparator.comparingInt(ItemTamanho::getOrdem))
                .map(t -> new TamanhoDto(t.getId(), t.getNome(), t.getPreco()))
                .toList();

        return new ItemAdminDto(
                item.getId(),
                item.getCategoria().getId(),
                item.getCategoria().getNome(),
                item.getCodigo(),
                item.getNome(),
                item.getDescricao(),
                item.getIngredientes(),
                item.getImagemUrl(),
                item.isDisponivel(),
                tamanhos);
    }

    /** Texto vazio ou só com espaços vira null. */
    private String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
