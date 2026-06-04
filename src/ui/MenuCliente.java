package ui;

import model.Cliente;
import service.ClienteService;

import java.util.List;
import java.util.Scanner;

/**
 * ATENÇÃO: Esta classe NÃO importa java.sql nem acessa o banco diretamente.
 * Todo acesso é feito via ClienteService.
 */
public class MenuCliente {

    private final ClienteService service = new ClienteService();
    private final Scanner        scanner;

    public MenuCliente(Scanner scanner) {
        this.scanner = scanner;
    }

    public void exibir() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n===== CLIENTES =====");
            System.out.println("1. Cadastrar cliente");
            System.out.println("2. Listar clientes");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> cadastrar();
                case "2" -> listar();
                case "0" -> voltar = true;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private void cadastrar() {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("E-mail: ");
        String email = scanner.nextLine();

        try {
            Cliente c = service.cadastrar(nome, email);
            System.out.println("✔ Cliente cadastrado: " + c);
        } catch (Exception e) {
            System.out.println("✘ Erro: " + e.getMessage());
        }
    }

    private void listar() {
        List<Cliente> clientes = service.listarTodos();
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        System.out.println();
        clientes.forEach(System.out::println);
    }
}
