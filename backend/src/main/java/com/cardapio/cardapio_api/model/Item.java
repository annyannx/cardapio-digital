package com.cardapio.cardapio_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Item do cardápio (uma pizza, uma esfiha, um refri, um suco...).
 * Representa a tabela "item" do banco.
 */
@Entity
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "ingredientes")
    private String ingredientes;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Column(name = "disponivel", nullable = false)
    private boolean disponivel;

    // Tamanhos e preços do item (Grande/Broto, Lata/600 ml, Único...).
    @OneToMany(mappedBy = "item")
    @OrderBy("ordem ASC")
    private List<ItemTamanho> tamanhos = new ArrayList<>();

    // Exigido pelo JPA.
    protected Item() {
    }

    public Long getId() {
        return id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public List<ItemTamanho> getTamanhos() {
        return tamanhos;
    }
}
