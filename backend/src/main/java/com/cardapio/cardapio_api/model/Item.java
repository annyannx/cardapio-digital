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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Item do cardápio (uma pizza, uma esfiha, um refri, um suco...).
 * Representa a tabela "item" do banco.
 *
 * Obs.: as colunas criado_em e atualizado_em são preenchidas pelo banco
 * e, por enquanto, não são mapeadas aqui.
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
    // cascade = ALL: salvar/excluir o item salva/exclui os tamanhos junto.
    // orphanRemoval = true: tamanho removido da lista é apagado do banco.
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<ItemTamanho> tamanhos = new ArrayList<>();

    // Exigido pelo JPA.
    protected Item() {
    }

    public Item(Categoria categoria, String codigo, String nome, String descricao,
                String ingredientes, String imagemUrl, boolean disponivel) {
        atualizar(categoria, codigo, nome, descricao, ingredientes, imagemUrl, disponivel);
    }

    /** Altera os dados do item (usado na criação e na edição). */
    public void atualizar(Categoria categoria, String codigo, String nome, String descricao,
                          String ingredientes, String imagemUrl, boolean disponivel) {
        this.categoria = categoria;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.ingredientes = ingredientes;
        this.imagemUrl = imagemUrl;
        this.disponivel = disponivel;
    }

    public void adicionarTamanho(ItemTamanho tamanho) {
        tamanho.setItem(this);
        tamanhos.add(tamanho);
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
