package model;

public class Cliente {

    private final int id;
    private final String nome;
    private final String email;

    // Construtor para criação (sem ID – gerado pelo banco)
    public Cliente(String nome, String email) {
        this(0, nome, email);
    }

    // Construtor completo – usado ao ler do banco (sem setters)
    public Cliente(int id, String nome, String email) {
        this.id    = id;
        this.nome  = nome;
        this.email = email;
    }

    public int    getId()    { return id; }
    public String getNome()  { return nome; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return String.format("Cliente[id=%d, nome='%s', email='%s']", id, nome, email);
    }
}
