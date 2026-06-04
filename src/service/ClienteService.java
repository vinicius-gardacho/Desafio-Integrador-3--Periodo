package service;

import exception.EntidadeNaoEncontradaException;
import model.Cliente;
import repository.ClienteRepository;
import util.Validador;

import java.util.List;

public class ClienteService {

    private final ClienteRepository repo = new ClienteRepository();

    public Cliente cadastrar(String nome, String email) {
        Validador.validarNomeNaoVazio(nome, "nome");
        Validador.validarEmail(email);
        return repo.salvar(new Cliente(nome.trim(), email.trim().toLowerCase()));
    }

    public Cliente buscarPorId(int id) {
        return repo.buscarPorId(id)
                   .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", id));
    }

    public List<Cliente> listarTodos() {
        return repo.listarTodos();
    }
}
