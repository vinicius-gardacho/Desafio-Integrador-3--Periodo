package repository;

import exception.PersistenciaException;
import model.Cliente;
import util.ConexaoBanco;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteRepository {

    public Cliente salvar(Cliente cliente) {
        String sql = "INSERT INTO clientes (nome, email) VALUES (?, ?)";
        try (Connection con = ConexaoBanco.obter();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getEmail());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Cliente(rs.getInt(1), cliente.getNome(), cliente.getEmail());
                }
            }
            throw new PersistenciaException("Falha ao obter ID gerado para Cliente.", null);

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new PersistenciaException("E-mail já cadastrado: " + cliente.getEmail(), e);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao salvar cliente.", e);
        }
    }

    public Optional<Cliente> buscarPorId(int id) {
        String sql = "SELECT id, nome, email FROM clientes WHERE id = ?";
        try (Connection con = ConexaoBanco.obter();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar cliente id=" + id, e);
        }
    }

    public List<Cliente> listarTodos() {
        String sql = "SELECT id, nome, email FROM clientes ORDER BY nome";
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = ConexaoBanco.obter();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar clientes.", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("email")
        );
    }
}
