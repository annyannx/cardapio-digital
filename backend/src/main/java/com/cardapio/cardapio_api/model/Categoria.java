package com.cardapio.cardapio_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Categoria do cardápio (Pizzas, Esfihas, Refris, Sucos).
 * Representa a tabela "categoria" do banco.
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    @Column(name = "ativa", nullable = false)
    private boolean ativa;

    // Itens desta categoria, ordenados pelo código e depois pelo nome.
    @OneToMany(mappedBy = "categoria")
    @OrderBy("codigo ASC, nome ASC")
    private List<Item> itens = new ArrayList<>();

    // Opcionais disponíveis nesta categoria (tabela categoria_adicional).
    @ManyToMany
    @JoinTable(
            name = "categoria_adicional",
            joinColumns = @JoinColumn(name = "categoria_id"),
            inverseJoinColumns = @JoinColumn(name = "adicional_id"))
    @OrderBy("nome ASC")
    private List<Adicional> adicionais = new ArrayList<>();

    // Exigido pelo JPA.
    protected Categoria() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getOrdem() {
        return ordem;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public List<Item> getItens() {
        return itens;
    }

    public List<Adicional> getAdicionais() {
        return adicionais;
    }
}
