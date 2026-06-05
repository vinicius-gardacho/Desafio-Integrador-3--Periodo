package repository;

import exception.PersistenciaException;
import model.Produto;
import model.enums.Categoria;
import util.ConexaoBanco;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoRepository {

    public Produto salvar(Produto produto) {
        String sql = "INSERT INTO produtos (nome, preco, estoque, categoria) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexaoBanco.obter();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, produto.getNome());
            ps.setBigDecimal(2, produto.getPreco());
            ps.setInt(3, produto.getEstoque());
            ps.setString(4, produto.getCategoria().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Produto(
                        rs.getInt(1),
                        produto.getNome(),
                        produto.getPreco(),
                        produto.getEstoque(),
                        produto.getCategoria()
                    );
                }
            }
            throw new PersistenciaException("Falha ao obter ID gerado para Produto.", null);

        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao salvar produto.", e);
        }
    }

    public Optional<Produto> buscarPorId(int id) {
        String sql = "SELECT id, nome, preco, estoque, categoria FROM produtos WHERE id = ?";
        try (Connection con = ConexaoBanco.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar produto id=" + id, e);
        }
    }

    public List<Produto> listarTodos() {
        String sql = "SELECT id, nome, preco, estoque, categoria FROM produtos ORDER BY nome";
        List<Produto> lista = new ArrayList<>();
        try (Connection con = ConexaoBanco.obter();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
            return lista;

        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar produtos.", e);
        }
    }

    /**
     * Decrementa o estoque de forma condicional e atômica no banco.
     * Retorna true se a atualização ocorreu (estoque era suficiente).
     * Chamado dentro de uma transação gerenciada pelo PedidoRepository.
     */
    public boolean decrementarEstoque(Connection con, int produtoId, int quantidade) throws SQLException {
        String sql = """
            UPDATE produtos
               SET estoque = estoque - ?
             WHERE id = ? AND estoque >= ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, produtoId);
            ps.setInt(3, quantidade);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Mapeamento ResultSet → objeto (sem setters)
    // -------------------------------------------------------------------------
    private Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getBigDecimal("preco"),
            rs.getInt("estoque"),
            Categoria.fromString(rs.getString("categoria"))
        );
    }
}
