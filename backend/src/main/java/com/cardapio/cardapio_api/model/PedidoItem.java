package com.cardapio.cardapio_api.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Item de um pedido. Guarda uma "foto" do nome, tamanho e preço no momento
 * da compra, para o histórico não mudar se o cardápio for alterado depois.
 * Representa a tabela "pedido_item" do banco.
 */
@Entity
@Table(name = "pedido_item")
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Referência ao item do cardápio (pode ficar nula se o item for excluído).
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_nome", nullable = false)
    private String itemNome;

    @Column(name = "tamanho_nome", nullable = false)
    private String tamanhoNome;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    @Column(name = "observacao")
    private String observacao;

    @OneToMany(mappedBy = "pedidoItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItemAdicional> adicionais = new ArrayList<>();

    // Exigido pelo JPA.
    protected PedidoItem() {
    }

    public PedidoItem(Long itemId, String itemNome, String tamanhoNome,
                      BigDecimal precoUnitario, int quantidade, String observacao) {
        this.itemId = itemId;
        this.itemNome = itemNome;
        this.tamanhoNome = tamanhoNome;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
        this.observacao = observacao;
    }

    // Visível só dentro do pacote "model" (quem chama é o Pedido).
    void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public void adicionarAdicional(PedidoItemAdicional adicional) {
        adicional.setPedidoItem(this);
        adicionais.add(adicional);
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getItemNome() {
        return itemNome;
    }

    public String getTamanhoNome() {
        return tamanhoNome;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public List<PedidoItemAdicional> getAdicionais() {
        return adicionais;
    }
}
