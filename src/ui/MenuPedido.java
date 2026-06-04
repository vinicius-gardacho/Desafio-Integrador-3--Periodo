package ui;

import model.Pedido;
import service.PedidoService;

import java.util.*;

/**
 * ATENÇÃO: Esta classe NÃO importa java.sql nem acessa o banco diretamente.
 */
public class MenuPedido {

    private final PedidoService service = new PedidoService();
    private final Scanner       scanner;

    public MenuPedido(Scanner scanner) {
        this.scanner = scanner;
    }

    public void exibir() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n===== PEDIDOS =====");
            System.out.println("1. Criar pedido");
            System.out.println("2. Listar pedidos");
            System.out.println("3. Buscar pedido por ID");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> criar();
                case "2" -> listar();
                case "3" -> buscarPorId();
                case "0" -> voltar = true;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private void criar() {
        try {
            System.out.print("ID do cliente: ");
            int clienteId = Integer.parseInt(scanner.nextLine().trim());

            Map<Integer, Integer> itens = new LinkedHashMap<>();
            boolean adicionando = true;

            while (adicionando) {
                System.out.print("ID do produto (0 para finalizar): ");
                int prodId = Integer.parseInt(scanner.nextLine().trim());
                if (prodId == 0) {
                    adicionando = false;
                    continue;
                }
                System.out.print("Quantidade: ");
                int qtd = Integer.parseInt(scanner.nextLine().trim());
                itens.put(prodId, itens.getOrDefault(prodId, 0) + qtd);
                System.out.println("  Item adicionado.");
            }

            Pedido pedido = service.criarPedido(clienteId, itens);
            System.out.println("✔ Pedido criado e enviado para a FILA:");
            System.out.println(pedido);

        } catch (Exception e) {
            System.out.println("✘ Erro: " + e.getMessage());
        }
    }

    private void listar() {
        List<Pedido> pedidos = service.listarTodos();
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido cadastrado.");
            return;
        }
        System.out.println();
        pedidos.forEach(p -> System.out.println(p + "\n"));
    }

    private void buscarPorId() {
        try {
            System.out.print("ID do pedido: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.println(service.buscarPorId(id));
        } catch (Exception e) {
            System.out.println("✘ Erro: " + e.getMessage());
        }
    }
}
