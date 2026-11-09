package com.cardapio.cardapio_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Opcional escolhido para um item do pedido (ex.: "Catupiry Original").
 * Guarda nome e preço no momento da compra.
 * Representa a tabela "pedido_item_adicional" do banco.
 */
@Entity
@Table(name = "pedido_item_adicional")
public class PedidoItemAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_item_id", nullable = false)
    private PedidoItem pedidoItem;

    @Column(name = "adicional_nome", nullable = false)
    private String adicionalNome;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    // Exigido pelo JPA.
    protected PedidoItemAdicional() {
    }

    public PedidoItemAdicional(String adicionalNome, BigDecimal preco) {
        this.adicionalNome = adicionalNome;
        this.preco = preco;
    }

    // Visível só dentro do pacote "model" (quem chama é o PedidoItem).
    void setPedidoItem(PedidoItem pedidoItem) {
        this.pedidoItem = pedidoItem;
    }

    public Long getId() {
        return id;
    }

    public String getAdicionalNome() {
        return adicionalNome;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
