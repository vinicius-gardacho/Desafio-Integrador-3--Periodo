package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilitário de conexão JDBC.
 * Adapte as constantes URL, USER e PASSWORD conforme seu ambiente local.
 */
public class ConexaoBanco {

    private static final String URL      = "jdbc:mysql://localhost:3306/gestao_pedidos?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String USER     = "Vinicius";
    private static final String PASSWORD = "Gardacho123";

    private ConexaoBanco() { /* impede instanciação */ }

    /**
     * Retorna uma nova conexão ao banco de dados.
     * O chamador é responsável por fechá-la (use try-with-resources).
     */
    public static Connection obter() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}



