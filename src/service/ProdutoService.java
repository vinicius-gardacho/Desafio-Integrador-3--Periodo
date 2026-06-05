package service;

import exception.EntidadeNaoEncontradaException;
import model.Produto;
import model.enums.Categoria;
import repository.ProdutoRepository;
import util.Validador;

import java.math.BigDecimal;
import java.util.List;

public class ProdutoService {

    private final ProdutoRepository repo = new ProdutoRepository();

    public Produto cadastrar(String nome, BigDecimal preco, int estoque, Categoria categoria) {
        Validador.validarNomeNaoVazio(nome, "nome");
        Validador.validarPrecoPositivo(preco);
        Validador.validarEstoqueNaoNegativo(estoque);
        return repo.salvar(new Produto(nome.trim(), preco, estoque, categoria));
    }

    public Produto buscarPorId(int id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto", id));
    }

    public List<Produto> listarTodos() {
        return repo.listarTodos();
    }
}
