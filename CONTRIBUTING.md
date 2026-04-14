# Contribuindo com o Lottus Mobile API

## Requisitos

- Java 21
- Docker e Docker Compose
- Maven 3.9+
- Git

## Setup do ambiente

1. Clone o repositorio:

```bash
git clone <url-do-repo>
cd mobile
```

2. Suba o banco:

```bash
docker compose up -d
```

3. Rode a aplicacao:

```bash
./mvnw spring-boot:run
```

4. Verifique no Swagger: `http://localhost:8080/swagger-ui/index.html`

## Estrutura do projeto

O projeto segue arquitetura **por funcionalidade** com camadas internas. Cada feature fica em seu proprio pacote:

```
feature/
├── controller/     Endpoints REST
├── service/        Logica de negocio
├── repository/     Acesso a dados (JPA)
├── entity/         Entidades JPA
└── command/        DTOs (request/response)
```

### Convencoes

- **DTOs** usam sufixo `Command` (ex: `RegisterCommand`, `LoginCommand`)
- **Services especializados** tem nome descritivo (ex: `AlunoVerificationService`, `RefreshTokenService`), nao apenas `FeatureService`
- **Sem comentarios** no codigo — o codigo deve ser autolegivel
- **Logs** via `@Slf4j` do Lombok
- **Injecao** via construtor com `@RequiredArgsConstructor`
- **Resposta padrao** via `ApiResponse<T>` para todos os endpoints
- **Excecoes de negocio** via `BusinessException` com HTTP status customizavel
- **Validacao** via Bean Validation (`@Valid`, `@NotBlank`, etc.)
- **Relacionamentos N:N** usam `@EmbeddedId` + `@Embeddable`

## Criando uma nova feature

1. Crie o pacote em `com.br.lottus.mobile.<feature>/`
2. Adicione as subcamadas necessarias (entity, repository, service, controller, command)
3. Crie a migration Flyway em `src/main/resources/db/migration/` seguindo a numeracao sequencial (`V8__`, `V9__`, etc.)
4. Documente os endpoints com anotacoes do Swagger (`@Operation`, `@Schema`, `@ApiResponses`)
5. Endpoints publicos devem ser adicionados no `SecurityConfig.securityFilterChain`

## Migrations (Flyway)

- Arquivos em `src/main/resources/db/migration/`
- Formato: `V{numero}__descricao.sql`
- **Nunca altere** uma migration ja aplicada — crie uma nova
- Use `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci` nas tabelas

## Seguranca

- Senhas sempre com `PasswordEncoder` (BCrypt)
- Nunca armazene tokens em texto plano — use hash SHA-256
- Novos endpoints protegidos por padrao; so libere em `SecurityConfig` se necessario
- Use `@AuthenticationPrincipal Usuario usuario` para obter o usuario logado

## Commits

- Mensagens claras e descritivas
- Uma feature/fix por commit
- Rode os testes antes de fazer push:

```bash
./mvnw test
```

## Pull Requests

- Crie a branch a partir de `main`
- Nomeie como `feature/<descricao>` ou `fix/<descricao>`
- Inclua descricao do que foi feito e como testar
- Aguarde review antes de mergear
