package ui;

import service.PedidoService;

import java.util.Scanner;

/**
 * ATENÇÃO: Esta classe NÃO importa java.sql nem acessa o banco diretamente.
 */
public class MenuRelatorio {

    private final PedidoService service = new PedidoService();
    private final Scanner       scanner;

    public MenuRelatorio(Scanner scanner) {
        this.scanner = scanner;
    }

    public void exibir() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n===== RELATÓRIOS =====");
            System.out.println("1. Pedidos finalizados por cliente (total e valor)");
            System.out.println("2. Produtos mais vendidos (top 10)");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> relatorio1();
                case "2" -> relatorio2();
                case "0" -> voltar = true;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private void relatorio1() {
        System.out.println("\n--- Pedidos Finalizados por Cliente ---");
        service.relatorioPedidosPorCliente().forEach(System.out::println);
    }

    private void relatorio2() {
        System.out.println("\n--- Produtos Mais Vendidos (Top 10) ---");
        service.relatorioProdutosMaisVendidos().forEach(System.out::println);
    }
}
