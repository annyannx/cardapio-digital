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
import java.math.RoundingMode;

/**
 * Tamanho/variação de um item, com seu preço.
 * Exemplos: pizza "Grande" e "Broto"; refri "Lata", "600 ml", "1 Litro".
 * Representa a tabela "item_tamanho" do banco.
 */
@Entity
@Table(name = "item_tamanho")
public class ItemTamanho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    // Exigido pelo JPA.
    protected ItemTamanho() {
    }

    public ItemTamanho(String nome, BigDecimal preco, int ordem) {
        this.nome = nome;
        this.preco = arredondar(preco);
        this.ordem = ordem;
    }

    /** Altera preço e posição de um tamanho existente. */
    public void atualizar(BigDecimal preco, int ordem) {
        this.preco = arredondar(preco);
        this.ordem = ordem;
    }

    // Visível só dentro do pacote "model" (quem chama é o Item).
    void setItem(Item item) {
        this.item = item;
    }

    private static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getOrdem() {
        return ordem;
    }
}
