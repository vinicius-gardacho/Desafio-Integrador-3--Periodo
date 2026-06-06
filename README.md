# Sistema de Gestão de Pedidos — Desafio Integrador 3º Período

**Centro Universitário Campo Real – Engenharia de Software – 2026**

---

## Pré-requisitos

| Ferramenta | Preferencialmente  |
|------------|--------------|
| Java JDK   | TEMURIN JDK  |
| MySQL      |  WORKBENCH   |

---

## 1. Configurar o banco de dados

Execute o script BANCO-DI fornecido pelo MySQL WorkBench.

---

## 2. Configurar a conexão

NÃO será necessario configurar a conexão, pois já foi deixado ela previamente configurada.

---

## 3. Compilar

```bash
# Na raiz do projeto
mkdir -p out

# Listar todos os .java
find src -name * ".java" > sources.txt

# Compilar
javac -cp "lib/mysql-connector-j-x.x.x.jar" -d out @sources.txt
```

---

## 4. Executar

```bash
java -cp "out:lib/mysql-connector-j-8.x.x.jar" src.Main
# Windows:
java -cp "out;lib/mysql-connector-j-8.x.x.jar" src.Main
```

---

## Decisões Arquiteturais

### Isolamento SQL do Console
Nenhuma classe do pacote `src.ui` importa `java.sql`.
O fluxo é: `MenuXxx → Service → Repository → JDBC`.
Isso cumpre o requisito de separação de camadas.

### Thread e Gerenciamento de Conexões
A `ProcessadorPedidos` é uma thread *daemon* que:
- Abre **sua própria** `Connection` a cada ciclo de processamento
- Usa `SELECT ... FOR UPDATE SKIP LOCKED` para garantir que dois processos nunca peguem o mesmo pedido
- Fecha a conexão ao final de cada ciclo, completamente isolada do menu principal

### Ausência de Setters (Object Calisthenics)
Todos os objetos de domínio (`Cliente`, `Produto`, `Pedido`, `ItemPedido`) são construídos via **construtores completos** ao serem lidos do banco. Nenhum `setter` é utilizado para popular objetos vindos do `ResultSet`.

### Transação e Estoque Seguro
Ao criar um pedido:
1. O estoque é decrementado via `UPDATE ... WHERE estoque >= quantidade` (condicional e atômico)
2. Se qualquer item falhar, toda a transação é revertida com `rollback()`
3. O pedido só é persistido se **todos** os itens tiverem estoque disponível

---

## Estrutura de Pacotes

```
DESAFIO-INTEGRADOR
├── .vscode
│   └── settings.json
├── lib
│   ├── mysql-connector-j-9.7.0.jar
│   ├── ojdbc17.jar
│   └── sqlite-jdbc-3.53.1.0.jar
├── src
│   ├── exception/          ← Exceções customizadas do domínio
│   │   ├── EmailInvalidoException.java
│   │   ├── EntidadeNaoEncontradaException.java
│   │   ├── EstoqueInsuficienteException.java
│   │   └── PersistenciaException.java
│   ├── model/
│   │   ├── enums/          ← Categoria, StatusPedido
│   │   │   ├── Categoria.java
│   │   │   └── StatusPedido.java
│   │   ├── Cliente.java
│   │   ├── Produto.java
│   │   ├── Pedido.java
│   │   └── ItemPedido.java
│   ├── repository/         ← Acesso JDBC (único lugar com java.sql)
│   │   ├── ClienteRepository.java
│   │   ├── PedidoRepository.java
│   │   └── ProdutoRepository.java
│   ├── service/            ← Regras de negócio e validações
│   │   ├── ClienteService.java
│   │   ├── PedidoService.java
│   │   └── ProdutoService.java
│   ├── thread/             ← ProcessadorPedidos (thread assíncrona)
│   │   └── ProcessadorPedidos.java
│   ├── ui/                 ← Menus de console (sem java.sql)
│   │   ├── MenuCliente.java
│   │   ├── MenuPedido.java
│   │   ├── MenuProduto.java
│   │   └── MenuRelatorio.java
│   ├── util/               ← ConexaoBanco, Validador
│   │   ├── ConexaoBanco.java
│   │   └── Validador.java
│   └── Main.java
├── BANCO-DI.sql
└── README.md
```
