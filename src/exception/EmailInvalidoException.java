package exception;

// Lançada quando o e-mail informado é inválido
public class EmailInvalidoException extends RuntimeException {
    public EmailInvalidoException(String email) {
        super("E-mail inválido: " + email);
    }
}
