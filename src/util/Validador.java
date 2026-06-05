package util;

import exception.EmailInvalidoException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public class Validador {

    private static final Pattern REGEX_EMAIL =
        Pattern.compile("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private Validador() {}

    public static void validarEmail(String email) {
        if (email == null || email.isBlank() || !REGEX_EMAIL.matcher(email).matches()) {
            throw new EmailInvalidoException(email);
        }
    }

    public static void validarNomeNaoVazio(String nome, String campo) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Campo '" + campo + "' não pode ser vazio.");
        }
    }

    public static void validarPrecoPositivo(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser positivo.");
        }
    }

    public static void validarEstoqueNaoNegativo(int estoque) {
        if (estoque < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo.");
        }
    }

    public static void validarQuantidadePositiva(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
    }
}
