package com.cardapio.cardapio_api.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido feito pelo cliente. O "id" é o número do pedido exibido ao cliente.
 * Representa a tabela "pedido" do banco.
 *
 * Obs.: as colunas de data/hora (criado_em, atualizado_em) são preenchidas
 * pelo próprio banco (DEFAULT now()). Vamos mapeá-las quando precisarmos
 * exibir a data/hora na lista de pedidos do administrador (RF007).
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_nome", nullable = false)
    private String clienteNome;

    @Column(name = "mesa")
    private String mesa;

    @Column(name = "observacao")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPedido status;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    // cascade = ALL: ao salvar o pedido, os itens e o histórico são salvos junto.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoStatusHistorico> historico = new ArrayList<>();

    // Exigido pelo JPA.
    protected Pedido() {
    }

    public Pedido(String clienteNome, String mesa, String observacao) {
        this.clienteNome = clienteNome;
        this.mesa = mesa;
        this.observacao = observacao;
        this.status = StatusPedido.RECEBIDO;
        this.valorTotal = BigDecimal.ZERO;
    }

    public void adicionarItem(PedidoItem item) {
        item.setPedido(this);
        itens.add(item);
    }

    public void definirValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    /** Registra no histórico uma mudança de status (anterior é nulo na criação). */
    public void registrarHistorico(StatusPedido anterior, StatusPedido novo, Long usuarioId) {
        historico.add(new PedidoStatusHistorico(this, anterior, novo, usuarioId));
    }

    public Long getId() {
        return id;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public String getMesa() {
        return mesa;
    }

    public String getObservacao() {
        return observacao;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public List<PedidoItem> getItens() {
        return itens;
    }

    public List<PedidoStatusHistorico> getHistorico() {
        return historico;
    }
}
