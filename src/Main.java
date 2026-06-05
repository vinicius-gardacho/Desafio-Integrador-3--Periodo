

import thread.ProcessadorPedidos;
import ui.*;

import java.util.Scanner;

/**
 * Ponto de entrada da aplicação.
 *
 * Arquitetura em camadas:
 *   Main → UI (menu*) → Service → Repository → Banco (JDBC)
 *
 * A thread ProcessadorPedidos roda em segundo plano e gerencia
 * suas próprias conexões, isolada da conexão do menu principal.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Inicia a thread de processamento em segundo plano
        ProcessadorPedidos processador = new ProcessadorPedidos();
        processador.start();

        // Menus (sem qualquer import de java.sql)
        MenuCliente   menuCliente   = new MenuCliente(scanner);
        MenuProduto   menuProduto   = new MenuProduto(scanner);
        MenuPedido    menuPedido    = new MenuPedido(scanner);
        MenuRelatorio menuRelatorio = new MenuRelatorio(scanner);

        boolean executando = true;
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║   Sistema de Gestão de Pedidos        ║");
        System.out.println("║   Campo Real – Engenharia de Software ║");
        System.out.println("╚═══════════════════════════════════════╝");

        while (executando) {
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1. Clientes");
            System.out.println("2. Produtos");
            System.out.println("3. Pedidos");
            System.out.println("4. Relatórios");
            System.out.println("0. Sair");
            System.out.print("Opção: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> menuCliente.exibir();
                case "2" -> menuProduto.exibir();
                case "3" -> menuPedido.exibir();
                case "4" -> menuRelatorio.exibir();
                case "0" -> {
                    executando = false;
                    processador.encerrar();
                    System.out.println("Encerrando sistema. Até logo!");
                }
                default -> System.out.println("Opção inválida.");
            }
        }
        scanner.close();
    }
}
