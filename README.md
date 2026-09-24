# 🚐 SafeWay — Backend

**SafeWay** é uma API REST desenvolvida em **Java 21 com Spring Boot 3.5** para auxiliar motoristas e gestores de
transporte escolar no controle diário de suas atividades.  
O sistema oferece funcionalidades de chamada de alunos, gestão financeira (mensalidades, pagamentos e despesas),
otimização de rotas com Google Maps, e muito mais.

---

## ✨ Funcionalidades

| Módulo           | Descrição                                                       |
|------------------|-----------------------------------------------------------------|
| **Autenticação** | Registro e login com JWT (RSA). Roles: `ADMIN` e `COMMON`       |
| **Alunos**       | CRUD de alunos vinculados a escolas e transportes               |
| **Escolas**      | Cadastro de escolas com endereço e nível de ensino              |
| **Responsáveis** | Gestão de responsáveis dos alunos                               |
| **Chamada**      | Controle de presença dos alunos no embarque/desembarque         |
| **Itinerários**  | Criação de itinerários com alunos e escolas associados          |
| **Rotas**        | Otimização de rotas via Google Maps / Route Optimization API    |
| **Transportes**  | Cadastro de veículos (modelo, placa, capacidade)                |
| **Endereços**    | Gerenciamento de endereços com geocodificação                   |
| **Financeiro**   | Mensalidades, pagamentos de funcionários e controle de despesas |
| **Usuários**     | Gestão de contas e perfis de acesso                             |

---

## 🏗 Arquitetura

O projeto segue uma arquitetura em camadas:

```
src/main/java/com/safeway/tech/
├── api/
│   ├── controllers/    # REST Controllers
│   └── dto/            # Data Transfer Objects (request/response)
├── client/             # Clients externos (Google Optimization, Feign)
├── config/             # Configurações (Security, Swagger, CORS, Google)
├── domain/
│   ├── models/         # Entidades JPA
│   └── enums/          # Enumerações (NivelEnsino, StatusChamada, UserRole...)
├── infra/
│   ├── exception/      # Tratamento global de exceções
│   └── messaging/      # RabbitMQ (config, publishers, events)
├── repository/         # Spring Data JPA Repositories + Specifications
└── service/
    ├── services/       # Interfaces de serviço
    ├── implementations/# Implementações dos serviços
    └── mappers/        # Conversores entidade ↔ DTO
```

---

## 🛠 Tecnologias

| Tecnologia                                   | Versão   | Propósito                             |
|----------------------------------------------|----------|---------------------------------------|
| **Java**                                     | 21       | Linguagem                             |
| **Spring Boot**                              | 3.5.5    | Framework principal                   |
| **Spring Security + OAuth2 Resource Server** | —        | Autenticação JWT (RSA)                |
| **Spring Data JPA**                          | —        | Persistência (ORM)                    |
| **Spring WebFlux**                           | —        | Chamadas HTTP reativas                |
| **Spring Cloud OpenFeign**                   | 2025.0.1 | Client HTTP declarativo               |
| **Spring AMQP**                              | —        | Mensageria com RabbitMQ               |
| **MySQL**                                    | latest   | Banco de dados em produção            |
| **H2**                                       | —        | Banco em memória (desenvolvimento)    |
| **Lombok**                                   | —        | Redução de boilerplate                |
| **SpringDoc OpenAPI**                        | 2.8.13   | Documentação Swagger UI               |
| **Google Maps Services**                     | 2.2.0    | Geocodificação e rotas                |
| **Maven**                                    | —        | Gerenciamento de dependências e build |
| **Docker**                                   | —        | Contêineres MySQL                     |

---

## ⚙ Configuração

```properties
# JWT — chaves RSA (já inclusas em src/main/resources/keys/)
jwt.public.key=classpath:keys/pub.key
jwt.private.key=classpath:keys/pri.key
# Banco de dados (H2 para dev ou MySQL para produção)
spring.datasource.url=jdbc:h2:mem:safewaydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
# Google Maps (opcional)
google.projectId=SEU_PROJECT_ID
google.maps.api.key=SEU_API_KEY
```

---

## 🔗 Endpoints principais

### Autenticação (`/auth`) — acesso público

| Método | Rota             | Descrição                    |
|--------|------------------|------------------------------|
| `POST` | `/auth/register` | Registrar novo usuário       |
| `POST` | `/auth/login`    | Autenticar e obter token JWT |

**Exemplo — Registro:**

POST /auth/register

```json
{
  "nome": "Usuario Teste",
  "email": "usuario@teste.com",
  "senha": "senha123",
  "tel1": "11999999999"
}
```

**Exemplo — Login:**

POST /auth/login

```json
{
  "email": "usuario@teste.com",
  "senha": "senha123"
}
```

---

## 🗄 Banco de dados

### Principais tabelas

`usuarios` · `alunos` · `escolas` · `enderecos` · `transportes` · `responsaveis` · `chamadas` ·
`itinerarios` · `mensalidades_aluno` · `pagamentos`

---

## 🐇 Mensageria (RabbitMQ)

O sistema utiliza **RabbitMQ** para eventos assíncronos relacionados a alunos:

| Exchange         | Routing Key        | Evento           |
|------------------|--------------------|------------------|
| `aluno.exchange` | `aluno.criado`     | Aluno cadastrado |
| `aluno.exchange` | `aluno.atualizado` | Aluno atualizado |
| `aluno.exchange` | `aluno.inativado`  | Aluno inativado  |

---

## 🗺 Google Maps API

O módulo de rotas utiliza as APIs do Google para:

- **Geocodificação** de endereços (latitude/longitude)
- **Otimização de rotas** via Cloud Fleet Routing / Route Optimization API

### Configuração

1. Crie um projeto no [Google Cloud Console](https://console.cloud.google.com/)
2. Ative as APIs **Geocoding**, **Directions** e **Route Optimization**
3. Gere uma **API Key** e um arquivo de **Service Account** (JSON)
4. Configure no `application.properties`:
   ```properties
   google.projectId=SEU_PROJECT_ID
   google.maps.api.key=SEU_API_KEY
   ```
5. Defina a variável de ambiente para o service account:
   ```bash
   set GOOGLE_APPLICATION_CREDENTIALS=caminho/para/credentials.json
   ```

> Se as credenciais do Google não estiverem configuradas, o restante da aplicação funciona normalmente — apenas os
> endpoints de rotas/otimização retornarão erro.

## Configurar chaves para JWT

1. Gerar a chave privada

    ```bashopenssl genrsa -out pri.key 2048```

2. Extrair a chave pública

   ```bashopenssl rsa -in pri.key -pubout -out pub.key```

---

## 📁 Estrutura do projeto

```
back-end/
├── docker/
│   └── docker-compose.yml          # MySQL containers
├── src/
│   ├── main/
│   │   ├── java/com/safeway/tech/  # Código-fonte Java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── keys/               # Chaves RSA (pub.key, pri.key)
│   └── test/                       # Testes
├── pom.xml                         # Dependências Maven
├── mvnw / mvnw.cmd                 # Maven Wrapper
└── README.md
```

---

## 📄 Licença

Este projeto é desenvolvido para fins acadêmicos.

---

## 🚀 Publicação da imagem no ECR

### 1) Login no ECR

```bash
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

aws ecr get-login-password --region "$AWS_REGION" \
   | docker login --username AWS --password-stdin "$ECR_REGISTRY"
```

### 2) Build e push da imagem do Core API

```bash
TAG=latest
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

docker build -t "$ECR_REGISTRY/safeway-core:$TAG" .
docker push "$ECR_REGISTRY/safeway-core:$TAG"
```

### 3) Atualizar rollout na infra

No projeto `safeway-infra`, confirme a imagem em `terraform.tfvars`:

```hcl
core_api_image = "<account>.dkr.ecr.us-east-1.amazonaws.com/safeway-core:latest"
```

Depois aplique:

```bash
cd ../safeway-infra
terraform apply
```