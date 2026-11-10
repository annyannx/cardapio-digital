package com.cardapio.cardapio_api.service;

import com.cardapio.cardapio_api.dto.PedidoAdminDtos.AcompanhamentoDto;
import com.cardapio.cardapio_api.event.PedidoEventos;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tempo real com SSE (Server-Sent Events): o servidor "empurra" avisos para quem
 * está com a conexão aberta, sem precisar ficar perguntando de tempos em tempos.
 *
 * Dois tipos de ouvinte:
 *  - o painel do administrador: recebe "pedido-novo" e "pedido-atualizado";
 *  - o cliente que acompanha um pedido específico: recebe "status".
 *
 * Os avisos só saem DEPOIS que o banco confirma a gravação (AFTER_COMMIT).
 */
@Component
public class PedidoStreamService {

    private final List<SseEmitter> administradores = new CopyOnWriteArrayList<>();
    private final Map<Long, List<SseEmitter>> clientesPorPedido = new ConcurrentHashMap<>();

    // ------------------------------------------------------------------
    // Abertura das conexões
    // ------------------------------------------------------------------

    /** Abre a conexão do painel do administrador. */
    public SseEmitter abrirParaAdministrador() {
        SseEmitter emissor = new SseEmitter(0L); // 0 = sem limite de tempo
        registrar(emissor, administradores);
        enviar(emissor, "conectado", Map.of("mensagem", "conectado"));
        return emissor;
    }

    /** Abre a conexão de um cliente que acompanha o pedido "numero". Já envia a situação atual. */
    public SseEmitter abrirParaCliente(Long numero, AcompanhamentoDto situacaoAtual) {
        SseEmitter emissor = new SseEmitter(0L);
        List<SseEmitter> lista = clientesPorPedido.computeIfAbsent(numero, k -> new CopyOnWriteArrayList<>());
        registrar(emissor, lista);
        enviar(emissor, "status", situacaoAtual);
        return emissor;
    }

    // ------------------------------------------------------------------
    // Eventos que chegam do restante do sistema
    // ------------------------------------------------------------------

    @TransactionalEventListener
    public void aoCriarPedido(PedidoEventos.Criado evento) {
        transmitir(administradores, "pedido-novo", evento.pedido());
    }

    @TransactionalEventListener
    public void aoAlterarStatus(PedidoEventos.StatusAlterado evento) {
        transmitir(administradores, "pedido-atualizado", evento.pedido());

        List<SseEmitter> clientes = clientesPorPedido.get(evento.pedido().numero());
        if (clientes != null) {
            transmitir(clientes, "status", evento.acompanhamento());
        }
    }

    // ------------------------------------------------------------------
    // "Batimento": a cada 25 s manda um sinal vazio para manter a conexão viva
    // e descobrir conexões que já caíram.
    // ------------------------------------------------------------------

    @Scheduled(fixedRate = 25000)
    public void batimento() {
        batimento(administradores);
        clientesPorPedido.values().forEach(this::batimento);
    }

    private void batimento(List<SseEmitter> lista) {
        for (SseEmitter emissor : lista) {
            try {
                emissor.send(SseEmitter.event().comment("keepalive"));
            } catch (IOException | IllegalStateException e) {
                lista.remove(emissor);
            }
        }
    }

    // ------------------------------------------------------------------
    // Utilitários
    // ------------------------------------------------------------------

    private void registrar(SseEmitter emissor, List<SseEmitter> lista) {
        lista.add(emissor);
        Runnable remover = () -> lista.remove(emissor);
        emissor.onCompletion(remover);
        emissor.onTimeout(remover);
        emissor.onError(erro -> remover.run());
    }

    private void transmitir(List<SseEmitter> lista, String nomeEvento, Object dados) {
        for (SseEmitter emissor : lista) {
            if (!enviar(emissor, nomeEvento, dados)) {
                lista.remove(emissor);
            }
        }
    }

    /** Envia um evento. Devolve false se a conexão já estava quebrada. */
    private boolean enviar(SseEmitter emissor, String nomeEvento, Object dados) {
        try {
            emissor.send(SseEmitter.event()
                    .name(nomeEvento)
                    .data(dados, MediaType.APPLICATION_JSON));
            return true;
        } catch (IOException | IllegalStateException e) {
            try {
                emissor.complete();
            } catch (RuntimeException ignorado) {
                // conexão já encerrada: nada a fazer
            }
            return false;
        }
    }
}
