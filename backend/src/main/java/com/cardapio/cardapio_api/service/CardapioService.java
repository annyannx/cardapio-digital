package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.CardapioDtos.AdicionalDto;
import com.cardapio.cardapio_api.dto.CardapioDtos.CategoriaDto;
import com.cardapio.cardapio_api.dto.CardapioDtos.ItemDto;
import com.cardapio.cardapio_api.dto.CardapioDtos.TamanhoDto;
import com.cardapio.cardapio_api.model.Adicional;
import com.cardapio.cardapio_api.model.Categoria;
import com.cardapio.cardapio_api.model.Item;
import com.cardapio.cardapio_api.model.ItemTamanho;
import com.cardapio.cardapio_api.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio do cardápio (RF001).
 */
@Service
public class CardapioService {

    private final CategoriaRepository categoriaRepository;

    public CardapioService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Devolve o cardápio completo: categorias ativas, cada uma com seus
     * itens disponíveis, tamanhos/preços e opcionais.
     *
     * O @Transactional é necessário porque os itens e tamanhos são carregados
     * sob demanda (lazy) e precisam da conexão aberta enquanto montamos o resultado.
     */
    @Transactional(readOnly = true)
    public List<CategoriaDto> listarCardapio() {
        return categoriaRepository.findByAtivaTrueOrderByOrdemAsc()
                .stream()
                .map(this::categoriaParaDto)
                .toList();
    }

    private CategoriaDto categoriaParaDto(Categoria categoria) {
        List<ItemDto> itens = categoria.getItens().stream()
                .filter(Item::isDisponivel)
                .map(this::itemParaDto)
                .toList();

        List<AdicionalDto> adicionais = categoria.getAdicionais().stream()
                .filter(Adicional::isAtivo)
                .map(this::adicionalParaDto)
                .toList();

        return new CategoriaDto(categoria.getId(), categoria.getNome(), itens, adicionais);
    }

    private ItemDto itemParaDto(Item item) {
        List<TamanhoDto> tamanhos = item.getTamanhos().stream()
                .map(this::tamanhoParaDto)
                .toList();

        return new ItemDto(
                item.getId(),
                item.getCodigo(),
                item.getNome(),
                item.getDescricao(),
                item.getIngredientes(),
                item.getImagemUrl(),
                tamanhos);
    }

    private TamanhoDto tamanhoParaDto(ItemTamanho tamanho) {
        return new TamanhoDto(tamanho.getId(), tamanho.getNome(), tamanho.getPreco());
    }

    private AdicionalDto adicionalParaDto(Adicional adicional) {
        return new AdicionalDto(adicional.getId(), adicional.getNome(), adicional.getPreco());
    }
}
