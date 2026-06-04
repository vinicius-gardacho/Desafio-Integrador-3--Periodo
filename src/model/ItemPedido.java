package model;

import java.math.BigDecimal;

public class ItemPedido {

    private final int        id;
    private final int        pedidoId;
    private final Produto    produto;
    private final int        quantidade;
    private final BigDecimal precoUnit; // snapshot do preço no momento do pedido

    // Construtor para criação (sem IDs gerados pelo banco)
    public ItemPedido(Produto produto, int quantidade) {
        this(0, 0, produto, quantidade, produto.getPreco());
    }

    // Construtor completo – leitura do banco (sem setters)
    public ItemPedido(int id, int pedidoId, Produto produto, int quantidade, BigDecimal precoUnit) {
        this.id         = id;
        this.pedidoId   = pedidoId;
        this.produto    = produto;
        this.quantidade = quantidade;
        this.precoUnit  = precoUnit;
    }

    public int        getId()         { return id; }
    public int        getPedidoId()   { return pedidoId; }
    public Produto    getProduto()    { return produto; }
    public int        getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnit()  { return precoUnit; }

    public BigDecimal getSubtotal() {
        return precoUnit.multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public String toString() {
        return String.format(
            "  -> %s | Qtd: %d | Unit: R$%.2f | Subtotal: R$%.2f",
            produto.getNome(), quantidade, precoUnit, getSubtotal()
        );
    }
}
