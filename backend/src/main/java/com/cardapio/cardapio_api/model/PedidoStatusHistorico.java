package com.cardapio.cardapio_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Registro de uma mudança de status de um pedido (RF008): de qual status
 * para qual, e qual usuário fez a mudança (nulo quando o pedido é criado
 * pelo cliente). A data/hora é preenchida pelo banco (alterado_em).
 * Representa a tabela "pedido_status_historico" do banco.
 */
@Entity
@Table(name = "pedido_status_historico")
public class PedidoStatusHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior")
    private StatusPedido statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false)
    private StatusPedido statusNovo;

    @Column(name = "usuario_id")
    private Long usuarioId;

    // Exigido pelo JPA.
    protected PedidoStatusHistorico() {
    }

    PedidoStatusHistorico(Pedido pedido, StatusPedido statusAnterior,
                          StatusPedido statusNovo, Long usuarioId) {
        this.pedido = pedido;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.usuarioId = usuarioId;
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public StatusPedido getStatusAnterior() {
        return statusAnterior;
    }

    public StatusPedido getStatusNovo() {
        return statusNovo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}
