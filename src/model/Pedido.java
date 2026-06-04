package model;

import model.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {

    private final int             id;
    private final Cliente         cliente;
    private final StatusPedido    status;
    private final LocalDateTime   criadoEm;
    private final List<ItemPedido> itens;

    // Construtor para criação (sem ID, sem data, sem itens ainda)
    public Pedido(Cliente cliente) {
        this(0, cliente, StatusPedido.ABERTO, LocalDateTime.now(), new ArrayList<>());
    }

    // Construtor completo – leitura do banco (sem setters)
    public Pedido(int id, Cliente cliente, StatusPedido status,
                    LocalDateTime criadoEm, List<ItemPedido> itens) {
        this.id       = id;
        this.cliente  = cliente;
        this.status   = status;
        this.criadoEm = criadoEm;
        this.itens    = new ArrayList<>(itens);
    }

    public int              getId()       { return id; }
    public Cliente          getCliente()  { return cliente; }
    public StatusPedido     getStatus()   { return status; }
    public LocalDateTime    getCriadoEm() { return criadoEm; }
    public List<ItemPedido> getItens()    { return Collections.unmodifiableList(itens); }

    public BigDecimal getTotal() {
        return itens.stream()
                    .map(ItemPedido::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
            "Pedido[id=%d, cliente='%s', status=%s, criado=%s, total=R$%.2f]",
            id, cliente.getNome(), status, criadoEm, getTotal()
        ));
        for (ItemPedido item : itens) {
            sb.append("\n").append(item);
        }
        return sb.toString();
    }
}
