package service;

import exception.EntidadeNaoEncontradaException;
import model.*;
import repository.PedidoRepository;
import util.Validador;

import java.util.List;
import java.util.Map;

public class PedidoService {

    private final PedidoRepository  pedidoRepo  = new PedidoRepository();
    private final ClienteService     clienteSvc  = new ClienteService();
    private final ProdutoService     produtoSvc  = new ProdutoService();

    /**
     * Cria e envia um pedido para a FILA.
     *
     * @param clienteId ID do cliente
     * @param itensSolicitados mapa produtoId → quantidade
     */
    public Pedido criarPedido(int clienteId, Map<Integer, Integer> itensSolicitados) {
        if (itensSolicitados == null || itensSolicitados.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve ter ao menos um item.");
        }

        Cliente cliente = clienteSvc.buscarPorId(clienteId);
        Pedido  pedido  = new Pedido(cliente);

        for (Map.Entry<Integer, Integer> entrada : itensSolicitados.entrySet()) {
            int produtoId  = entrada.getKey();
            int quantidade = entrada.getValue();
            Validador.validarQuantidadePositiva(quantidade);

            Produto produto = produtoSvc.buscarPorId(produtoId);
            pedido.getItens(); // acesso à lista para verificar imutabilidade; itens adicionados abaixo
        }

        // Reconstrói o pedido com todos os itens para entrar na transação
        Pedido pedidoCompleto = montarPedidoComItens(cliente, itensSolicitados);
        return pedidoRepo.salvar(pedidoCompleto); // transação + decremento de estoque atomicamente
    }

    public Pedido buscarPorId(int id) {
        return pedidoRepo.buscarPorId(id)
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Pedido", id));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepo.listarTodos();
    }

    public List<String> relatorioPedidosPorCliente() {
        return pedidoRepo.relatorioPedidosPorCliente();
    }

    public List<String> relatorioProdutosMaisVendidos() {
        return pedidoRepo.relatorioProdutosMaisVendidos();
    }

    // -------------------------------------------------------------------------
    private Pedido montarPedidoComItens(Cliente cliente, Map<Integer, Integer> itensSolicitados) {
        List<ItemPedido> itens = new java.util.ArrayList<>();
        for (Map.Entry<Integer, Integer> entrada : itensSolicitados.entrySet()) {
            Produto produto = produtoSvc.buscarPorId(entrada.getKey());
            itens.add(new ItemPedido(produto, entrada.getValue()));
        }
        return new Pedido(
            0,
            cliente,
            model.enums.StatusPedido.ABERTO,
            java.time.LocalDateTime.now(),
            itens
        );
    }
}
