package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilitário de conexão JDBC.
 * Adapte as constantes URL, USER e PASSWORD conforme seu ambiente local.
 */
public class ConexaoBanco {

    private static final String URL      = "jdbc:mysql://localhost:3306/gestao_pedidos?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private ConexaoBanco() { }

    
    public static Connection obter() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}



