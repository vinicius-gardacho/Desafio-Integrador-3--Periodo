package thread;

import util.ConexaoBanco;

import java.sql.*;

/**
 * Thread de processamento assíncrono de pedidos.
 *
 * Ciclo de vida:
 *  1. Busca um pedido com status FILA (SELECT ... FOR UPDATE SKIP LOCKED)
 *  2. Muda para PROCESSANDO na mesma transação (garante exclusão mútua)
 *  3. Simula processamento (Thread.sleep)
 *  4. Muda para FINALIZADO
 *  5. Abre e fecha sua própria conexão a cada ciclo – isolada do menu principal
 */
public class ProcessadorPedidos extends Thread {

    private static final int INTERVALO_MS   = 3_000; // pausa entre ciclos
    private static final int PROCESSAMENTO_MS = 5_000; // simula trabalho

    private volatile boolean rodando = true;

    public ProcessadorPedidos() {
        setDaemon(true); // não impede o encerramento da JVM
        setName("thread-processador");
    }

    public void encerrar() {
        rodando = false;
        interrupt();
    }

    @Override
    public void run() {
        System.out.println("[Processador] Thread iniciada.");
        while (rodando) {
            try {
                processarProximoPedido();
                Thread.sleep(INTERVALO_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[Processador] Thread encerrada.");
    }

    // -------------------------------------------------------------------------
    // Cada ciclo abre e fecha sua própria conexão
    // -------------------------------------------------------------------------
    private void processarProximoPedido() {
        // SELECT ... FOR UPDATE SKIP LOCKED garante que outra thread não pegue o mesmo pedido
        String sqlBuscarWithSkip = """
            SELECT id FROM pedidos
            WHERE status = 'FILA'
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """;

        String sqlBuscar = """
            SELECT id FROM pedidos
            WHERE status = 'FILA'
            LIMIT 1
            FOR UPDATE
            """;
        String sqlProcessando = "UPDATE pedidos SET status = 'PROCESSANDO' WHERE id = ? AND status = 'FILA'";
        String sqlFinalizado  = "UPDATE pedidos SET status = 'FINALIZADO'  WHERE id = ?";

        try (Connection con = ConexaoBanco.obter()) {
            con.setAutoCommit(false);

            Integer pedidoId = null;

            // Tenta usar SKIP LOCKED (MySQL 8+). Se não suportado, faz fallback sem SKIP LOCKED.
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlBuscarWithSkip);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pedidoId = rs.getInt("id");
                    }
                }
            } catch (SQLException e) {
                // Erro de sintaxe provável (servidor antigo/MariaDB sem SKIP LOCKED)
                try (PreparedStatement ps = con.prepareStatement(sqlBuscar);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pedidoId = rs.getInt("id");
                    }
                }
            }

            if (pedidoId == null) {
                con.rollback();
                return; // nenhum pedido na fila
            }

            // Muda para PROCESSANDO (dentro da mesma transação – lock ainda ativo)
            try (PreparedStatement ps = con.prepareStatement(sqlProcessando)) {
                ps.setInt(1, pedidoId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    return; // outra thread pegou antes (raro com SKIP LOCKED, mas seguro)
                }
            }

            con.commit(); // libera o lock; pedido já está como PROCESSANDO

            System.out.printf("[Processador] Pedido #%d: PROCESSANDO...%n", pedidoId);

            // Simula trabalho pesado (fora da transação)
            try {
                Thread.sleep(PROCESSAMENTO_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Finaliza em nova transação
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlFinalizado)) {
                ps.setInt(1, pedidoId);
                ps.executeUpdate();
            }
            con.commit();

            System.out.printf("[Processador] Pedido #%d: FINALIZADO.%n", pedidoId);

        } catch (SQLException e) {
            System.err.println("[Processador] Erro ao processar pedido: " + e.getMessage());
        }
    }
}
