package com.cardapio.cardapio_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Opcional/adicional que o cliente pode acrescentar (Catupiry, Bacon, Champignon...).
 * Representa a tabela "adicional" do banco.
 */
@Entity
@Table(name = "adicional")
public class Adicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    // Exigido pelo JPA.
    protected Adicional() {
    }

    public Adicional(String nome, BigDecimal preco, boolean ativo) {
        atualizar(nome, preco, ativo);
    }

    /** Altera os dados do opcional (usado na criação e na edição pelo painel). */
    public void atualizar(String nome, BigDecimal preco, boolean ativo) {
        this.nome = nome;
        this.preco = preco.setScale(2, RoundingMode.HALF_UP);
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
