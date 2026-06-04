package model.enums;

public enum Categoria {
    ALIMENTOS,
    ELETRONICOS,
    LIVROS;

    public static Categoria fromString(String valor) {
        for (Categoria c : values()) {
            if (c.name().equalsIgnoreCase(valor)) return c;
        }
        throw new IllegalArgumentException("Categoria inválida: " + valor);
    }
}
