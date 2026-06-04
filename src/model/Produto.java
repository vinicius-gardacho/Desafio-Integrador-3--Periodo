package model;

import model.enums.Categoria;
import java.math.BigDecimal;

public class Produto {

    private final int        id;
    private final String     nome;
    private final BigDecimal preco;
    private final int        estoque;
    private final Categoria  categoria;

    // Construtor para criação (sem ID)
    public Produto(String nome, BigDecimal preco, int estoque, Categoria categoria) {
        this(0, nome, preco, estoque, categoria);
    }

    // Construtor completo – leitura do banco (sem setters)
    public Produto(int id, String nome, BigDecimal preco, int estoque, Categoria categoria) {
        this.id        = id;
        this.nome      = nome;
        this.preco     = preco;
        this.estoque   = estoque;
        this.categoria = categoria;
    }

    public int        getId()       { return id; }
    public String     getNome()     { return nome; }
    public BigDecimal getPreco()    { return preco; }
    public int        getEstoque()  { return estoque; }
    public Categoria  getCategoria(){ return categoria; }

    @Override
    public String toString() {
        return String.format(
            "Produto[id=%d, nome='%s', preco=R$%.2f, estoque=%d, categoria=%s]",
            id, nome, preco, estoque, categoria
        );
    }
}
