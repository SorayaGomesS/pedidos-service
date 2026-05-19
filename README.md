# pedidos-service

Microsserviço responsável pelo gerenciamento do ciclo de vida de pedidos na plataforma de e-commerce.

---

## 1. Descrição Funcional

**Nome:** `pedidos-service`

**Objetivo:** Gerenciar a criação, consulta e atualização de pedidos realizados pelos clientes da plataforma. O serviço é o ponto central da jornada de compra, orquestrando a comunicação com os serviços de Estoque, Pagamentos e Notificações.

**Responsabilidades principais:**
- Registrar novos pedidos com seus respectivos itens
- Consultar preço e disponibilidade de produtos junto ao serviço de Estoque
- Controlar o ciclo de status de cada pedido (PENDENTE → CONFIRMADO → EM_PROCESSAMENTO → ENVIADO → ENTREGUE)
- Publicar eventos de domínio para outros microsserviços via message broker
- Permitir o cancelamento de pedidos dentro das regras de negócio definidas

---

## 2. Endpoints da API

Base URL: `http://localhost:8081/api/v1`

| Método | URL | Descrição |
|--------|-----|-----------|
| `POST` | `/pedidos` | Cria um novo pedido |
| `GET` | `/pedidos/{id}` | Busca um pedido pelo ID |
| `GET` | `/pedidos?clienteId={id}` | Lista todos os pedidos de um cliente |
| `PATCH` | `/pedidos/{id}/status` | Atualiza o status de um pedido |
| `GET` | `/health` | Health check do serviço |
| `GET` | `/swagger-ui.html` | Documentação interativa (Swagger UI) |
| `GET` | `/api-docs` | Especificação OpenAPI em JSON |

---

## 3. Exemplos de Requisição e Resposta

### POST `/api/v1/pedidos` — Criar pedido

**Requisição:**
```json
{
  "clienteId": "cli-abc123",
  "itens": [
    {
      "produtoId": "prod-001",
      "quantidade": 2
    },
    {
      "produtoId": "prod-007",
      "quantidade": 1
    }
  ]
}
```

**Resposta (201 Created):**
```json
{
  "id": "ped-f3a12b9c",
  "clienteId": "cli-abc123",
  "status": "PENDENTE",
  "itens": [
    {
      "produtoId": "prod-001",
      "nomeProduto": "Teclado Mecânico",
      "quantidade": 2,
      "precoUnitario": 249.90,
      "subtotal": 499.80
    },
    {
      "produtoId": "prod-007",
      "nomeProduto": "Mouse sem fio",
      "quantidade": 1,
      "precoUnitario": 149.90,
      "subtotal": 149.90
    }
  ],
  "valorTotal": 649.70,
  "criadoEm": "2025-05-19T10:30:00",
  "atualizadoEm": null
}
```

---

### GET `/api/v1/pedidos/{id}` — Buscar pedido

**Resposta (200 OK):**
```json
{
  "id": "ped-f3a12b9c",
  "clienteId": "cli-abc123",
  "status": "CONFIRMADO",
  "itens": [...],
  "valorTotal": 649.70,
  "criadoEm": "2025-05-19T10:30:00",
  "atualizadoEm": "2025-05-19T10:45:00"
}
```

**Resposta (404 Not Found):**
```json
{
  "timestamp": "2025-05-19T10:30:00",
  "error": "Pedido não encontrado: ped-xyz999"
}
```

---

### PATCH `/api/v1/pedidos/{id}/status` — Atualizar status

**Requisição:**
```json
{
  "status": "CONFIRMADO"
}
```

**Resposta (200 OK):**
```json
{
  "id": "ped-f3a12b9c",
  "status": "CONFIRMADO",
  ...
}
```

**Resposta (400 Bad Request — transição inválida):**
```json
{
  "timestamp": "2025-05-19T10:31:00",
  "error": "Não é possível alterar um pedido com status: ENTREGUE"
}
```

---

## 4. Dependências Externas

### Microsserviços consumidos

| Serviço | Comunicação | Finalidade |
|---------|-------------|------------|
| `estoque-service` | HTTP REST (GET `/produtos/{id}`) | Obter nome e preço dos produtos ao criar pedido |
| `pagamentos-service` | Evento (consumer: `pagamento.aprovado`) | Confirmar pedido após aprovação do pagamento |

### Banco de Dados

| Recurso | Tecnologia | Detalhes |
|---------|-----------|----------|
| Banco relacional | PostgreSQL 16 | Host: `pedidos-postgres:5432`, DB: `pedidosdb` |

### Broker de Mensagens

| Recurso | Tecnologia | Detalhes |
|---------|-----------|----------|
| Message Broker | RabbitMQ 3 | Host: `rabbitmq:5672`, Exchange: `pedidos.exchange` |

### APIs Externas

Nenhuma API externa é consumida diretamente por este serviço.

---

## 5. Responsável pelo Serviço

| Campo | Informação |
|-------|------------|
| **Equipe** | Squad Checkout |
| **Desenvolvedor** | Soraya Gomes da Silva|
| **Contato** | soraya.gomes@aluno.ifsp.edu.br |
| **Canal no Slack** | `#squad-checkout` |

---

## 6. Procedimentos Básicos de Operação

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker e Docker Compose

### Executar localmente com Docker Compose

```bash
# Subir todos os serviços (PostgreSQL + RabbitMQ + pedidos-service)
docker-compose up -d

# Verificar se os containers estão rodando
docker-compose ps

# Parar os serviços
docker-compose down
```

### Executar localmente sem Docker (apenas a aplicação)

```bash
# 1. Garantir que PostgreSQL e RabbitMQ estejam rodando
# 2. Exportar variáveis de ambiente (ou editar application.properties)
export DB_URL=jdbc:postgresql://localhost:5432/pedidosdb
export DB_USER=postgres
export DB_PASS=postgres

# 3. Compilar e executar
mvn clean spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8081`

### Como verificar logs

```bash
# Logs em tempo real (Docker)
docker logs -f pedidos-service

# Logs com filtro de nível
docker logs pedidos-service 2>&1 | grep ERROR

# Logs locais (sem Docker)
# Os logs aparecem no console. Para salvar em arquivo, redirecione a saída:
mvn spring-boot:run > logs/pedidos.log 2>&1
```

### Health Check

```bash
curl http://localhost:8081/health
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "service": "pedidos-service"
}
```

O Actuator do Spring Boot também expõe informações detalhadas:

```bash
curl http://localhost:8081/actuator/health
```

### Como reiniciar o serviço

```bash
# Com Docker Compose
docker-compose restart pedidos-service

# Rebuild completo após mudança de código
docker-compose up -d --build pedidos-service
```

---

## 7. Regras de Negócio

### Criação de Pedido
- O campo `clienteId` é obrigatório e não pode ser vazio.
- O pedido deve conter **ao menos um item**.
- A **quantidade** de cada item deve ser maior que zero.
- O preço e nome do produto são obtidos no momento da criação a partir do `estoque-service` — não é permitido informar o preço manualmente.
- O `valorTotal` é calculado automaticamente como a soma dos subtotais de cada item.
- Todo pedido é criado com o status inicial `PENDENTE`.

### Atualização de Status
- As transições de status permitidas seguem o fluxo:

```
PENDENTE → CONFIRMADO → EM_PROCESSAMENTO → ENVIADO → ENTREGUE
                 ↓
             CANCELADO (de qualquer estado, exceto ENTREGUE)
```

- Pedidos com status `ENTREGUE` ou `CANCELADO` **não podem ter seu status alterado**.
- Não é permitido retroceder o status para `PENDENTE`.

### Cancelamento
- O cancelamento dispara o evento `pedido.cancelado`, que sinaliza ao `estoque-service` para reverter a reserva.

---

## 8. Eventos Publicados e Consumidos

### Eventos Publicados (Producer)

| Evento | Exchange | Routing Key | Descrição |
|--------|----------|-------------|-----------|
| `PedidoCriadoEvent` | `pedidos.exchange` | `pedido.criado` | Disparado quando um novo pedido é registrado com sucesso |
| `PedidoConfirmadoEvent` | `pedidos.exchange` | `pedido.confirmado` | Disparado quando o status muda para CONFIRMADO |
| `PedidoCanceladoEvent` | `pedidos.exchange` | `pedido.cancelado` | Disparado quando o pedido é cancelado |

**Exemplo de payload — `pedido.criado`:**
```json
{
  "pedidoId": "ped-f3a12b9c",
  "clienteId": "cli-abc123",
  "valorTotal": 649.70
}
```

### Eventos Consumidos (Consumer)

| Evento | Fila | Origem | Ação realizada |
|--------|------|--------|----------------|
| `pagamento.aprovado` | `pedidos.pagamento-aprovado` | `pagamentos-service` | Altera status do pedido para CONFIRMADO |

---

## 9. Métricas Monitoradas

As métricas são expostas via Spring Boot Actuator em `/actuator/metrics`.

| Métrica | Descrição | Alerta sugerido |
|---------|-----------|-----------------|
| `pedidos.criados.total` | Número total de pedidos criados | Queda > 30% vs. média histórica |
| `pedidos.cancelados.total` | Número de pedidos cancelados | Taxa > 10% dos criados |
| `http.server.requests` | Latência das requisições HTTP | P99 > 1000ms |
| `jvm.memory.used` | Uso de memória da JVM | > 80% do heap |
| `db.connections.active` | Conexões ativas com PostgreSQL | > 80% do pool |
| `rabbitmq.queue.messages` | Mensagens pendentes na fila | > 500 mensagens |

**Exemplo de consulta:**
```bash
curl http://localhost:8081/actuator/metrics/http.server.requests
```

---

## 10. ADR — Decisão Arquitetural Relacionada

### ADR-001: Uso de RabbitMQ para comunicação assíncrona entre microsserviços

**Status:** Aceito

**Contexto:**
Na arquitetura de microsserviços do projeto, os serviços de Pedidos, Pagamentos e Estoque precisam se comunicar de forma desacoplada. Uma chamada HTTP síncrona criaria acoplamento temporal e aumentaria a fragilidade do sistema.

**Decisão:**
Utilizar RabbitMQ como message broker para comunicação assíncrona entre os serviços. Cada serviço publica eventos de domínio ao seu próprio exchange, e os consumidores se inscrevem nas filas de interesse.

**Consequências:**
- **Positivas:** Desacoplamento entre serviços, maior resiliência a falhas temporárias, facilidade de escalar consumidores independentemente.
- **Negativas:** Eventual consistency — o sistema não é imediatamente consistente; requer monitoramento adicional de filas mortas (DLQ).

---

## Estrutura do Projeto

```
pedidos-service/
├── src/
│   └── main/
│       ├── java/com/example/pedidos/
│       │   ├── controller/       # Endpoints REST
│       │   ├── service/          # Regras de negócio
│       │   ├── repository/       # Acesso ao banco de dados
│       │   ├── model/            # Entidades JPA
│       │   ├── dto/              # Objetos de transferência de dados
│       │   ├── events/           # Publicadores de eventos
│       │   └── exception/        # Exceções e handler global
│       └── resources/
│           └── application.properties
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## Licença

Projeto acadêmico — uso educacional.
