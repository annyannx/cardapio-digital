package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.PedidoDtos.AdicionalPedidoResponse;
import com.cardapio.cardapio_api.dto.PedidoDtos.ItemPedidoRequest;
import com.cardapio.cardapio_api.dto.PedidoDtos.ItemPedidoResponse;
import com.cardapio.cardapio_api.dto.PedidoDtos.PedidoRequest;
import com.cardapio.cardapio_api.dto.PedidoDtos.PedidoResponse;
import com.cardapio.cardapio_api.model.Adicional;
import com.cardapio.cardapio_api.model.Item;
import com.cardapio.cardapio_api.model.ItemTamanho;
import com.cardapio.cardapio_api.model.Pedido;
import com.cardapio.cardapio_api.model.PedidoItem;
import com.cardapio.cardapio_api.model.PedidoItemAdicional;
import com.cardapio.cardapio_api.model.StatusPedido;
import com.cardapio.cardapio_api.repository.AdicionalRepository;
import com.cardapio.cardapio_api.repository.ItemTamanhoRepository;
import com.cardapio.cardapio_api.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regras de negócio da criação de pedidos (RF004).
 *
 * Regra de ouro: o cliente envia apenas IDs e quantidades. Nomes e preços
 * são sempre buscados no banco, e o total é calculado aqui no servidor.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemTamanhoRepository itemTamanhoRepository;
    private final AdicionalRepository adicionalRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ItemTamanhoRepository itemTamanhoRepository,
                         AdicionalRepository adicionalRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemTamanhoRepository = itemTamanhoRepository;
        this.adicionalRepository = adicionalRepository;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Pedido pedido = new Pedido(
                request.clienteNome().trim(),
                limpar(request.mesa()),
                limpar(request.observacao()));

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequest itemRequest : request.itens()) {
            ItemTamanho tamanho = itemTamanhoRepository.findById(itemRequest.tamanhoId())
                    .orElseThrow(() -> erro("Tamanho não encontrado: " + itemRequest.tamanhoId()));

            Item item = tamanho.getItem();
            if (!item.isDisponivel() || !item.getCategoria().isAtiva()) {
                throw erro("O item \"" + item.getNome() + "\" não está disponível");
            }

            PedidoItem pedidoItem = new PedidoItem(
                    item.getId(),
                    item.getNome(),
                    tamanho.getNome(),
                    tamanho.getPreco(),
                    itemRequest.quantidade(),
                    limpar(itemRequest.observacao()));

            // Preço de uma unidade = preço do tamanho + opcionais escolhidos.
            BigDecimal precoUnidade = tamanho.getPreco();
            for (Adicional adicional : buscarAdicionais(itemRequest.adicionaisIds(), item)) {
                pedidoItem.adicionarAdicional(
                        new PedidoItemAdicional(adicional.getNome(), adicional.getPreco()));
                precoUnidade = precoUnidade.add(adicional.getPreco());
            }

            total = total.add(precoUnidade.multiply(BigDecimal.valueOf(itemRequest.quantidade())));
            pedido.adicionarItem(pedidoItem);
        }

        pedido.definirValorTotal(total);
        // Primeira linha do histórico: o pedido nasceu como RECEBIDO (sem usuário admin).
        pedido.registrarHistorico(null, StatusPedido.RECEBIDO, null);

        Pedido salvo = pedidoRepository.save(pedido);
        return paraResponse(salvo);
    }

    /** Busca os opcionais pedidos e confere se existem, estão ativos e valem para a categoria do item. */
    private List<Adicional> buscarAdicionais(List<Long> ids, Item item) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Long> idsUnicos = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (idsUnicos.isEmpty()) {
            return List.of();
        }

        List<Adicional> encontrados = adicionalRepository.findAllById(idsUnicos);
        if (encontrados.size() != idsUnicos.size()) {
            throw erro("Um ou mais opcionais não foram encontrados");
        }

        Set<Long> permitidos = item.getCategoria().getAdicionais().stream()
                .filter(Adicional::isAtivo)
                .map(Adicional::getId)
                .collect(Collectors.toSet());

        for (Adicional adicional : encontrados) {
            if (!permitidos.contains(adicional.getId())) {
                throw erro("O opcional \"" + adicional.getNome()
                        + "\" não está disponível para \"" + item.getNome() + "\"");
            }
        }
        return encontrados;
    }

    private PedidoResponse paraResponse(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(this::itemParaResponse)
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getStatus().name(),
                pedido.getValorTotal(),
                pedido.getClienteNome(),
                pedido.getMesa(),
                itens);
    }

    private ItemPedidoResponse itemParaResponse(PedidoItem pedidoItem) {
        List<AdicionalPedidoResponse> adicionais = pedidoItem.getAdicionais().stream()
                .map(a -> new AdicionalPedidoResponse(a.getAdicionalNome(), a.getPreco()))
                .toList();

        BigDecimal precoUnidade = pedidoItem.getPrecoUnitario();
        for (AdicionalPedidoResponse adicional : adicionais) {
            precoUnidade = precoUnidade.add(adicional.preco());
        }
        BigDecimal subtotal = precoUnidade.multiply(BigDecimal.valueOf(pedidoItem.getQuantidade()));

        return new ItemPedidoResponse(
                pedidoItem.getItemNome(),
                pedidoItem.getTamanhoNome(),
                pedidoItem.getPrecoUnitario(),
                pedidoItem.getQuantidade(),
                subtotal,
                pedidoItem.getObservacao(),
                adicionais);
    }

    /** Texto vazio ou só com espaços vira null. */
    private String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
