package exception;

public class EntidadeNaoEncontradaException extends RuntimeException {
    public EntidadeNaoEncontradaException(String entidade, int id) {
        super(entidade + " com id=" + id + " não encontrado(a).");
    }
}
