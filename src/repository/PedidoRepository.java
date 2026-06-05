package repository;

import exception.EstoqueInsuficienteException;
import exception.PersistenciaException;
import model.*;
import model.enums.StatusPedido;
import util.ConexaoBanco;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoRepository {

    private final ClienteRepository  clienteRepo  = new ClienteRepository();
    private final ProdutoRepository  produtoRepo  = new ProdutoRepository();

    // -------------------------------------------------------------------------
    // Salva pedido + itens numa única transação; decrementa estoque atomicamente
    // -------------------------------------------------------------------------
    public Pedido salvar(Pedido pedido) {
        String sqlPedido = "INSERT INTO pedidos (cliente_id, status) VALUES (?, ?)";
        String sqlItem   = "INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unit) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexaoBanco.obter()) {
            con.setAutoCommit(false);
            try {
                // 1. Verifica e decrementa estoque de cada item (atualização condicional)
                for (ItemPedido item : pedido.getItens()) {
                    boolean ok = produtoRepo.decrementarEstoque(
                        con,
                        item.getProduto().getId(),
                        item.getQuantidade()
                    );
                    if (!ok) {
                        // Busca estoque atual para montar mensagem de erro
                        int estoqueAtual = buscarEstoqueAtual(con, item.getProduto().getId());
                        throw new EstoqueInsuficienteException(
                            item.getProduto().getNome(),
                            item.getQuantidade(),
                            estoqueAtual
                        );
                    }
                }

                // 2. Insere o pedido
                int pedidoId;
                try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, pedido.getCliente().getId());
                    ps.setString(2, StatusPedido.FILA.name()); // sempre salvo como FILA
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new PersistenciaException("ID do pedido não gerado.", null);
                        pedidoId = rs.getInt(1);
                    }
                }

                // 3. Insere os itens
                try (PreparedStatement ps = con.prepareStatement(sqlItem)) {
                    for (ItemPedido item : pedido.getItens()) {
                        ps.setInt(1, pedidoId);
                        ps.setInt(2, item.getProduto().getId());
                        ps.setInt(3, item.getQuantidade());
                        ps.setBigDecimal(4, item.getPrecoUnit());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();

                // Retorna pedido com ID e status correto
                return new Pedido(pedidoId, pedido.getCliente(), StatusPedido.FILA,
                                  LocalDateTime.now(), pedido.getItens());

            } catch (EstoqueInsuficienteException e) {
                con.rollback();
                throw e;
            } catch (SQLException e) {
                con.rollback();
                throw new PersistenciaException("Erro ao salvar pedido – transação revertida.", e);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao abrir conexão para salvar pedido.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Busca um pedido com seus itens
    // -------------------------------------------------------------------------
    public Optional<Pedido> buscarPorId(int id) {
        String sql = """
            SELECT p.id, p.cliente_id, p.status, p.criado_em
              FROM pedidos p
             WHERE p.id = ?
            """;
        try (Connection con = ConexaoBanco.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente         cliente = clienteRepo.buscarPorId(rs.getInt("cliente_id")).orElse(null);
                    StatusPedido    status  = StatusPedido.fromString(rs.getString("status"));
                    LocalDateTime   criado  = rs.getTimestamp("criado_em").toLocalDateTime();
                    List<ItemPedido> itens  = buscarItensDoPedido(id);
                    return Optional.of(new Pedido(id, cliente, status, criado, itens));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar pedido id=" + id, e);
        }
    }

    // -------------------------------------------------------------------------
    // Lista todos os pedidos
    // -------------------------------------------------------------------------
    public List<Pedido> listarTodos() {
        String sql = "SELECT id, cliente_id, status, criado_em FROM pedidos ORDER BY id DESC";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexaoBanco.obter();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int           pedidoId = rs.getInt("id");
                Cliente       cliente  = clienteRepo.buscarPorId(rs.getInt("cliente_id")).orElse(null);
                StatusPedido  status   = StatusPedido.fromString(rs.getString("status"));
                LocalDateTime criado   = rs.getTimestamp("criado_em").toLocalDateTime();
                List<ItemPedido> itens = buscarItensDoPedido(pedidoId);
                lista.add(new Pedido(pedidoId, cliente, status, criado, itens));
            }
            return lista;

        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar pedidos.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Relatório 1 – Total de pedidos e valor por cliente
    // -------------------------------------------------------------------------
    public List<String> relatorioPedidosPorCliente() {
        String sql = """
            SELECT c.nome                          AS cliente,
                   COUNT(p.id)                     AS total_pedidos,
                   SUM(ip.quantidade * ip.preco_unit) AS valor_total
              FROM clientes c
              JOIN pedidos p       ON p.cliente_id = c.id
              JOIN itens_pedido ip ON ip.pedido_id = p.id
             WHERE p.status = 'FINALIZADO'
             GROUP BY c.id, c.nome
             ORDER BY valor_total DESC
            """;
        List<String> linhas = new ArrayList<>();
        try (Connection con = ConexaoBanco.obter();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            linhas.add(String.format("%-30s %15s %15s", "Cliente", "Pedidos", "Total (R$)"));
            linhas.add("-".repeat(62));
            while (rs.next()) {
                linhas.add(String.format("%-30s %15d %15.2f",
                    rs.getString("cliente"),
                    rs.getInt("total_pedidos"),
                    rs.getBigDecimal("valor_total")
                ));
            }
            return linhas;

        } catch (SQLException e) {
            throw new PersistenciaException("Erro no relatório por cliente.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Relatório 2 – Produtos mais vendidos (por quantidade)
    // -------------------------------------------------------------------------
    public List<String> relatorioProdutosMaisVendidos() {
        String sql = """
            SELECT pr.nome                AS produto,
                   pr.categoria           AS categoria,
                   SUM(ip.quantidade)     AS total_vendido,
                   AVG(ip.preco_unit)     AS preco_medio
              FROM produtos pr
              JOIN itens_pedido ip ON ip.produto_id = pr.id
              JOIN pedidos p       ON p.id = ip.pedido_id
             WHERE p.status = 'FINALIZADO'
             GROUP BY pr.id, pr.nome, pr.categoria
             ORDER BY total_vendido DESC
             LIMIT 10
            """;
        List<String> linhas = new ArrayList<>();
        try (Connection con = ConexaoBanco.obter();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            linhas.add(String.format("%-30s %-15s %12s %15s", "Produto", "Categoria", "Qtd Vendida", "Preço Médio"));
            linhas.add("-".repeat(75));
            while (rs.next()) {
                linhas.add(String.format("%-30s %-15s %12d %15.2f",
                    rs.getString("produto"),
                    rs.getString("categoria"),
                    rs.getInt("total_vendido"),
                    rs.getBigDecimal("preco_medio")
                ));
            }
            return linhas;

        } catch (SQLException e) {
            throw new PersistenciaException("Erro no relatório de produtos mais vendidos.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Auxiliares internos
    // -------------------------------------------------------------------------
    private List<ItemPedido> buscarItensDoPedido(int pedidoId) throws SQLException {
        String sql = """
            SELECT ip.id, ip.produto_id, ip.quantidade, ip.preco_unit,
                pr.nome, pr.preco, pr.estoque, pr.categoria
                FROM itens_pedido ip
                JOIN produtos pr ON pr.id = ip.produto_id
                WHERE ip.pedido_id = ?
            """;
        List<ItemPedido> itens = new ArrayList<>();
        try (Connection con = ConexaoBanco.obter();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto(
                        rs.getInt("produto_id"),
                        rs.getString("nome"),
                        rs.getBigDecimal("preco"),
                        rs.getInt("estoque"),
                        model.enums.Categoria.fromString(rs.getString("categoria"))
                    );
                    itens.add(new ItemPedido(
                        rs.getInt("id"),
                        pedidoId,
                        produto,
                        rs.getInt("quantidade"),
                        rs.getBigDecimal("preco_unit")
                    ));
                }
            }
        }
        return itens;
    }

    private int buscarEstoqueAtual(Connection con, int produtoId) throws SQLException {
        String sql = "SELECT estoque FROM produtos WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, produtoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("estoque") : 0;
            }
        }
    }
}
