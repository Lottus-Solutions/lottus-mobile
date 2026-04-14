# Lottus Mobile API

API REST para controle de leituras escolares. Permite que pais (usuarios) acompanhem emprestimos de livros de seus filhos (alunos) atraves de um aplicativo mobile.

## Stack

| Tecnologia | Versao |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.4 |
| Spring Security | JWT + OAuth2 (Google OIDC) |
| MySQL | 8.0 |
| Flyway | Migrations versionadas |
| Swagger | springdoc-openapi 2.8.6 |
| Lombok | 1.18.36 |
| JJWT | 0.12.6 |

## Arquitetura

Pacotes organizados **por funcionalidade**, com camadas internas (controller, service, repository, entity, command):

```
com.br.lottus.mobile
├── auth/           Registro, login, JWT, refresh token
├── usuario/        Perfil do pai/responsavel
├── aluno/          Verificacao de matricula (RA)
├── turma/          Entidade turma
├── livro/          Entidade livro
├── emprestimo/     Emprestimos e status
├── common/         ApiResponse<T>, BusinessException, GlobalExceptionHandler
└── config/
    ├── security/   SecurityConfig, JwtService, JwtAuthenticationFilter
    └── oauth2/     Google OIDC, CustomOidcUserService, SuccessHandler
```

## Pre-requisitos

- Java 21
- Docker e Docker Compose
- Maven 3.9+

## Setup

### 1. Subir o banco de dados

```bash
docker compose up -d
```

Isso cria o MySQL com:
- **Database:** lottusdb
- **User:** lottus-devuser
- **Password:** 40028922
- **Porta:** 3306

### 2. Configurar variaveis de ambiente

Para OAuth2 Google (opcional para dev local):

```bash
export GOOGLE_CLIENT_ID=seu-client-id
export GOOGLE_CLIENT_SECRET=seu-client-secret
```

Para JWT (opcional, possui default para dev):

```bash
export JWT_SECRET=sua-chave-base64
```

### 3. Rodar a aplicacao

```bash
./mvnw spring-boot:run
```

A API estara disponivel em `http://localhost:8080`.

### 4. Acessar o Swagger

```
http://localhost:8080/swagger-ui/index.html
```

## Endpoints principais

### Publicos (sem autenticacao)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/api/auth/register` | Registrar usuario (pai) vinculado a um aluno |
| POST | `/api/auth/login` | Login com email e senha |
| POST | `/api/auth/refresh` | Renovar tokens (rotacao automatica) |
| GET | `/api/alunos/verificar-ra/{matricula}` | Verificar existencia de RA |
| GET | `/oauth2/authorization/google` | Login via Google (OIDC) |

### Protegidos (Bearer token)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/usuarios/me` | Perfil do usuario autenticado |
| PATCH | `/api/usuarios/me` | Atualizar nome/avatar |
| POST | `/api/auth/logout` | Revogar refresh tokens |

## Fluxo de cadastro

1. Front consulta `GET /api/alunos/verificar-ra/{matricula}` para verificar se o RA existe e se o aluno ja esta vinculado
2. Front envia `POST /api/auth/register` com nome, email, senha e matricula do aluno
3. Backend valida, criptografa a senha (BCrypt), cria o usuario e vincula ao aluno via tabela N:N
4. Retorna access token + refresh token

## Fluxo OAuth2 Google

1. Front redireciona para `GET /oauth2/authorization/google`
2. Usuario autentica no Google
3. Backend processa o retorno, cria/atualiza o usuario
4. Redireciona para `http://localhost:5173/oauth2/callback?accessToken=...&refreshToken=...`

## Seguranca

- Senhas criptografadas com **BCrypt**
- Access token JWT com expiracao de **15 minutos**
- Refresh token com **rotacao automatica** e **deteccao de reuso** (revoga todos se detectar reutilizacao)
- Apenas o **hash SHA-256** do refresh token e armazenado no banco
- CORS liberado apenas para `localhost:5173`
- Endpoints protegidos retornam **401** (sem redirect para Google)

## Testes

```bash
./mvnw test
```

## Estrutura do banco

Migrations Flyway em `src/main/resources/db/migration/`:

| Migration | Tabela |
|---|---|
| V1 | turmas |
| V2 | alunos |
| V3 | usuarios |
| V4 | usuario_aluno (N:N com EmbeddedId) |
| V5 | livros |
| V6 | emprestimos |
| V7 | refresh_tokens |
