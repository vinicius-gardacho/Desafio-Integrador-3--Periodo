package ui;

import model.Produto;
import model.enums.Categoria;
import service.ProdutoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * ATENÇÃO: Esta classe NÃO importa java.sql nem acessa o banco diretamente.
 */
public class MenuProduto {

    private final ProdutoService service = new ProdutoService();
    private final Scanner        scanner;

    public MenuProduto(Scanner scanner) {
        this.scanner = scanner;
    }

    public void exibir() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n===== PRODUTOS =====");
            System.out.println("1. Cadastrar produto");
            System.out.println("2. Listar produtos");
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
        try {
            System.out.print("Nome: ");
            String nome = scanner.nextLine();

            System.out.print("Preço (ex: 19.90): ");
            BigDecimal preco = new BigDecimal(scanner.nextLine().trim().replace(',', '.'));

            System.out.print("Estoque: ");
            int estoque = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Categoria: ");
            for (Categoria c : Categoria.values()) {
                System.out.println("  " + c.ordinal() + ". " + c.name());
            }
            System.out.print("Escolha: ");
            int idx = Integer.parseInt(scanner.nextLine().trim());
            Categoria categoria = Categoria.values()[idx];

            Produto p = service.cadastrar(nome, preco, estoque, categoria);
            System.out.println("✔ Produto cadastrado: " + p);

        } catch (Exception e) {
            System.out.println("✘ Erro: " + e.getMessage());
        }
    }

    private void listar() {
        List<Produto> produtos = service.listarTodos();
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        System.out.println();
        produtos.forEach(System.out::println);
    }
}
