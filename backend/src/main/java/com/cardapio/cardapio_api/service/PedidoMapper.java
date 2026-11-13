package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.PedidoAdminDtos.AcompanhamentoDto;
import com.cardapio.cardapio_api.dto.PedidoAdminDtos.HistoricoDto;
import com.cardapio.cardapio_api.dto.PedidoAdminDtos.HistoricoPublicoDto;
import com.cardapio.cardapio_api.dto.PedidoAdminDtos.PedidoAdminDto;
import com.cardapio.cardapio_api.dto.PedidoDtos.AdicionalPedidoResponse;
import com.cardapio.cardapio_api.dto.PedidoDtos.ItemPedidoResponse;
import com.cardapio.cardapio_api.model.Pedido;
import com.cardapio.cardapio_api.model.PedidoItem;
import com.cardapio.cardapio_api.model.PedidoStatusHistorico;
import com.cardapio.cardapio_api.model.Usuario;
import com.cardapio.cardapio_api.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converte um Pedido (entidade do banco) nos formatos devolvidos pela API.
 * Deve ser chamado dentro de uma transação, pois lê dados carregados sob demanda.
 */
@Component
public class PedidoMapper {

    private static final ZoneId FUSO_BRASILIA = ZoneId.of("America/Sao_Paulo");

    private final UsuarioRepository usuarioRepository;

    public PedidoMapper(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Visão completa, para o administrador (RF007/RF008). */
    public PedidoAdminDto paraAdminDto(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(this::itemParaResponse)
                .toList();

        Map<Long, String> nomes = nomesDosUsuarios(pedido);

        List<HistoricoDto> historico = historicoOrdenado(pedido).stream()
                .map(h -> new HistoricoDto(
                        h.getStatusAnterior() == null ? null : h.getStatusAnterior().name(),
                        h.getStatusNovo().name(),
                        nomeDoUsuario(h.getUsuarioId(), nomes),
                        formatar(h.getAlteradoEm())))
                .toList();

        return new PedidoAdminDto(
                pedido.getId(),
                pedido.getStatus().name(),
                pedido.getClienteNome(),
                pedido.getMesa(),
                pedido.getObservacao(),
                pedido.getValorTotal(),
                formatar(pedido.getCriadoEm()),
                formatar(pedido.getAtualizadoEm()),
                formatar(pedido.getConfirmadoEm()),
                itens,
                historico);
    }

    /** Visão para o cliente acompanhar o pedido (sem dados pessoais). */
    public AcompanhamentoDto paraAcompanhamento(Pedido pedido) {
        List<HistoricoPublicoDto> historico = historicoOrdenado(pedido).stream()
                .map(h -> new HistoricoPublicoDto(h.getStatusNovo().name(), formatar(h.getAlteradoEm())))
                .toList();

        return new AcompanhamentoDto(
                pedido.getId(),
                pedido.getStatus().name(),
                pedido.getValorTotal(),
                formatar(pedido.getCriadoEm()),
                formatar(pedido.getAtualizadoEm()),
                formatar(pedido.getConfirmadoEm()),
                historico);
    }

    // ------------------------------------------------------------------

    private List<PedidoStatusHistorico> historicoOrdenado(Pedido pedido) {
        return pedido.getHistorico().stream()
                .sorted(Comparator.comparing(PedidoStatusHistorico::getAlteradoEm))
                .toList();
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

    /** Busca, de uma vez só, os nomes dos usuários que aparecem no histórico. */
    private Map<Long, String> nomesDosUsuarios(Pedido pedido) {
        List<Long> ids = pedido.getHistorico().stream()
                .map(PedidoStatusHistorico::getUsuarioId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> nomes = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Usuario usuario : usuarioRepository.findAllById(ids)) {
                nomes.put(usuario.getId(), usuario.getNome());
            }
        }
        return nomes;
    }

    private String nomeDoUsuario(Long usuarioId, Map<Long, String> nomes) {
        if (usuarioId == null) {
            return "Cliente";
        }
        return nomes.getOrDefault(usuarioId, "Usuário removido");
    }

    private String formatar(Instant instante) {
        if (instante == null) {
            return null;
        }
        return instante.atZone(FUSO_BRASILIA).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
