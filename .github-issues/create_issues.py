#!/usr/bin/env python3
"""
Cria labels e issues no repositorio Lottus-Solutions/lottus-mobile via `gh`.

Fonte unica de verdade: a lista ISSUES e a lista LABELS definidas abaixo.
Execucao idempotente:
  - Labels: pula se ja existir (gh label create --force atualiza cor/descricao).
  - Issues: pula se ja existir issue (qualquer estado) com o mesmo titulo.

Uso:
  python create_issues.py                 # cria labels + issues
  python create_issues.py --dry-run       # so imprime o que faria
  python create_issues.py --only-labels   # so cria/atualiza labels
  python create_issues.py --export-csv    # escreve issues.csv ao lado

Requer `gh` autenticado com escopo `repo`.
"""
from __future__ import annotations

import argparse
import csv
import json
import os
import subprocess
import sys
from pathlib import Path

REPO = "Lottus-Solutions/lottus-mobile"

# ---------------------------------------------------------------------------
# Labels
# ---------------------------------------------------------------------------
LABELS = [
    # type
    {"name": "type:epic",     "color": "5319e7", "description": "Epic / agrupador de trabalho"},
    {"name": "type:feature",  "color": "0e8a16", "description": "Nova funcionalidade"},
    {"name": "type:chore",    "color": "cfd3d7", "description": "Infra, tooling, refator tecnico"},
    {"name": "type:docs",     "color": "0075ca", "description": "Documentacao"},
    {"name": "type:test",     "color": "fbca04", "description": "Testes"},
    # area
    {"name": "area:auth",         "color": "1d76db", "description": "Autenticacao e conta do pai"},
    {"name": "area:vinculo",      "color": "1d76db", "description": "Vinculo pai - aluno"},
    {"name": "area:alunos",       "color": "1d76db", "description": "Perfil do aluno (visao do pai)"},
    {"name": "area:livros",       "color": "1d76db", "description": "Catalogo de livros"},
    {"name": "area:emprestimos",  "color": "1d76db", "description": "Emprestimos / leituras"},
    {"name": "area:metas",        "color": "1d76db", "description": "Metas de leitura"},
    {"name": "area:infra",        "color": "1d76db", "description": "Infraestrutura / cross-cutting"},
    # priority
    {"name": "priority:P0", "color": "b60205", "description": "Critico - bloqueia outras tarefas"},
    {"name": "priority:P1", "color": "d93f0b", "description": "Alta prioridade"},
    {"name": "priority:P2", "color": "fbca04", "description": "Normal"},
    # track
    {"name": "track:A",      "color": "c5def5", "description": "Dev A (auth, vinculo, emprestimos)"},
    {"name": "track:B",      "color": "c2e0c6", "description": "Dev B (livros, metas)"},
    {"name": "track:shared", "color": "e4e669", "description": "Pode ser pego por qualquer dev"},
]

# ---------------------------------------------------------------------------
# Helpers para montar o body em formato padronizado
# ---------------------------------------------------------------------------
def body(
    *,
    contexto: str,
    escopo: list[str],
    aceite: list[str],
    tecnicas: list[str] | None = None,
    depende_de: list[str] | None = None,
    epic: str,
    track: str,
) -> str:
    parts = [f"## Contexto\n{contexto.strip()}\n"]
    parts.append("## Escopo")
    parts.extend(f"- {s}" for s in escopo)
    parts.append("")
    parts.append("## Criterios de aceite")
    parts.extend(f"- [ ] {s}" for s in aceite)
    parts.append("")
    if tecnicas:
        parts.append("## Notas tecnicas")
        parts.extend(f"- {s}" for s in tecnicas)
        parts.append("")
    if depende_de:
        parts.append("## Depende de")
        parts.extend(f"- {s}" for s in depende_de)
        parts.append("")
    parts.append(f"---\n**Epic:** `{epic}` | **Track:** `{track}`")
    return "\n".join(parts)


# ---------------------------------------------------------------------------
# Issues
# Ordem intencional: EPICs primeiro, depois infra, auth, vinculo, livros,
# emprestimos, metas, qualidade. Dentro de cada track as dependencias sao
# sequenciais; entre tracks, paralelas.
# ---------------------------------------------------------------------------
ISSUES: list[dict] = [
    # ==========================================================
    # EPICS
    # ==========================================================
    {
        "title": "[EPIC] Infraestrutura, documentacao e qualidade",
        "labels": ["type:epic", "area:infra", "priority:P0", "track:shared"],
        "body": body(
            epic="infra", track="shared",
            contexto=(
                "Agrupa o setup inicial do projeto: build, CI, docker, Swagger, tratamento de erros, "
                "logging e convencoes. Base para qualquer feature funcionar de forma confiavel."
            ),
            escopo=[
                "Docker Compose para dev (MySQL + app).",
                "GitHub Actions para build + testes.",
                "Swagger/OpenAPI com autenticacao JWT documentada.",
                "README do projeto.",
                "ApiResponse / GlobalExceptionHandler padronizados.",
                "Testes unitarios e de integracao dos fluxos principais.",
            ],
            aceite=[
                "Todas as issues filhas desta epic estao concluidas.",
                "Pipeline verde no main.",
                "Documentacao minima suficiente para onboarding de novo dev.",
            ],
        ),
    },
    {
        "title": "[EPIC] Autenticacao e conta do pai",
        "labels": ["type:epic", "area:auth", "priority:P0", "track:A"],
        "body": body(
            epic="auth", track="A",
            contexto=(
                "O usuario principal do app mobile e o pai/responsavel. Precisamos de cadastro, login JWT, "
                "refresh, login Google (OAuth2), recuperacao de senha e gestao do proprio perfil."
            ),
            escopo=[
                "Entidade Usuario + migration.",
                "Registro, login, refresh.",
                "Login Google (OAuth2).",
                "Esqueci minha senha / reset.",
                "Perfil do pai (me).",
            ],
            aceite=[
                "Todas as issues filhas concluidas.",
                "Fluxo completo testavel via Swagger.",
            ],
        ),
    },
    {
        "title": "[EPIC] Vinculo pai - aluno",
        "labels": ["type:epic", "area:vinculo", "priority:P0", "track:A"],
        "body": body(
            epic="vinculo", track="A",
            contexto=(
                "O pai so pode visualizar dados de alunos vinculados a ele. Cada pai pode ter N filhos; "
                "o vinculo e criado explicitamente informando matricula (e um codigo de validacao do aluno, "
                "definido na modelagem do endpoint)."
            ),
            escopo=[
                "Criar, listar e remover vinculos.",
                "Filtro/aspect de autorizacao aplicavel em alunos, emprestimos e metas.",
            ],
            aceite=[
                "Pai so enxerga dados de alunos vinculados.",
                "Tentativa de acesso cruzado retorna 403.",
            ],
        ),
    },
    {
        "title": "[EPIC] Catalogo de livros",
        "labels": ["type:epic", "area:livros", "priority:P1", "track:B"],
        "body": body(
            epic="livros", track="B",
            contexto=(
                "Catalogo de livros apenas para leitura (o pai nao cadastra livros, apenas registra a leitura "
                "de um livro ja existente para o filho). Independe dos demais tracks, pode rodar em paralelo."
            ),
            escopo=[
                "Entidade Livro completa (autor, sinopse, capa, categoria).",
                "Listagem/pesquisa.",
                "Detalhes.",
            ],
            aceite=[
                "Endpoints GET /livros e GET /livros/{id} disponiveis.",
                "Busca por titulo/autor funciona.",
            ],
        ),
    },
    {
        "title": "[EPIC] Leituras (emprestimos) do aluno",
        "labels": ["type:epic", "area:emprestimos", "priority:P0", "track:A"],
        "body": body(
            epic="emprestimos", track="A",
            contexto=(
                "Historico de leituras do aluno: cadastro de nova leitura (nao de novo livro), livro atual em "
                "leitura, conclusao/devolucao, historico completo. Usado tambem como base para progresso de metas."
            ),
            escopo=[
                "Modelagem revisada.",
                "POST nova leitura.",
                "Historico.",
                "Livro atual.",
                "Concluir leitura.",
            ],
            aceite=[
                "Pai consegue registrar leitura para um filho vinculado.",
                "Historico e livro atual carregam corretamente.",
            ],
        ),
    },
    {
        "title": "[EPIC] Metas de leitura",
        "labels": ["type:epic", "area:metas", "priority:P0", "track:B"],
        "body": body(
            epic="metas", track="B",
            contexto=(
                "O pai define metas para o filho (ex.: 'Ler 4 livros', 'Ler 1 livro de fantasia'). Cada meta tem "
                "tipo, titulo, duracao (inicio-fim) e forma de validacao (booleana 0/1 ou percentual sobre um "
                "valor alvo). Progresso pode ser atualizado manualmente ou automaticamente por integracao com "
                "emprestimos."
            ),
            escopo=[
                "Modelagem da entidade Meta.",
                "CRUD de metas.",
                "Integracao com emprestimos para progresso automatico.",
            ],
            aceite=[
                "Pai cria, lista, atualiza e remove metas para filhos vinculados.",
                "Progresso atualizado automaticamente quando emprestimo se encaixa na regra.",
            ],
        ),
    },

    # ==========================================================
    # INFRA (shared)
    # ==========================================================
    {
        "title": "chore(infra): validar docker-compose dev (MySQL + app) e application.yml",
        "labels": ["type:chore", "area:infra", "priority:P0", "track:shared"],
        "body": body(
            epic="infra", track="shared",
            contexto=(
                "O projeto ja tem docker-compose.yml e application.yml no scaffold. Precisamos garantir que "
                "`docker compose up` sobe MySQL + app do zero e que perfis `dev` e `prod` estao corretos."
            ),
            escopo=[
                "Revisar docker-compose.yml (healthcheck do MySQL, volumes, variaveis).",
                "Revisar application.yml / application-dev.yml / application-prod.yml.",
                "Documentar variaveis de ambiente obrigatorias.",
            ],
            aceite=[
                "`docker compose up --build` sobe o app do zero em maquina limpa.",
                "Variaveis sensiveis (JWT secret, DB password) vem de env, nao hardcoded.",
                "Flyway aplica todas as migrations na subida.",
            ],
        ),
    },
    {
        "title": "chore(infra): configurar GitHub Actions (build + test)",
        "labels": ["type:chore", "area:infra", "priority:P0", "track:shared"],
        "body": body(
            epic="infra", track="shared",
            contexto=(
                "Ja existe um workflow inicial (commit f03c4fc). Revisar e garantir que build + testes rodam em "
                "PR para main e disparam status check obrigatorio."
            ),
            escopo=[
                "Workflow roda em push/PR para main.",
                "Cacheia dependencias Maven.",
                "Reporta falha em teste/compilacao.",
            ],
            aceite=[
                "PR sem build verde nao pode ser mergeado (branch protection configurado).",
                "Tempo total < 5 min em PR medio.",
            ],
        ),
    },
    {
        "title": "chore(infra): configurar Swagger/OpenAPI com autenticacao JWT",
        "labels": ["type:chore", "area:infra", "priority:P1", "track:shared"],
        "body": body(
            epic="infra", track="shared",
            contexto=(
                "SwaggerConfig ja existe no scaffold. Completar para documentar JWT (Bearer), OAuth2 e todos os "
                "endpoints agrupados por tag."
            ),
            escopo=[
                "Security scheme JWT Bearer configurado.",
                "Tag por modulo (auth, vinculo, alunos, livros, emprestimos, metas).",
                "Exemplos de request/response onde faz sentido.",
            ],
            aceite=[
                "`/swagger-ui.html` acessivel.",
                "`Authorize` aceita JWT e o envia nos endpoints protegidos.",
            ],
        ),
    },
    {
        "title": "docs: README do projeto mobile (escopo, arquitetura, como rodar)",
        "labels": ["type:docs", "area:infra", "priority:P1", "track:shared"],
        "body": body(
            epic="infra", track="shared",
            contexto=(
                "README atual e o default do Spring Initializr. Precisa explicar o escopo (API do app mobile do "
                "pai), relacao com `lottus-backend`, arquitetura em camadas e como rodar localmente."
            ),
            escopo=[
                "Visao geral e publico-alvo (pai/responsavel).",
                "Arquitetura e dependencias principais.",
                "Como rodar (docker + mvn).",
                "Convencoes de branch/commit/PR (1 PR por issue).",
            ],
            aceite=[
                "Novo dev consegue subir o ambiente seguindo so o README.",
            ],
        ),
    },
    {
        "title": "chore(infra): padronizar ApiResponse, GlobalExceptionHandler e validacao",
        "labels": ["type:chore", "area:infra", "priority:P1", "track:shared"],
        "body": body(
            epic="infra", track="shared",
            contexto=(
                "Ja existem `ApiResponse`, `BusinessException` e `GlobalExceptionHandler`. Fechar o contrato "
                "para que todos os controllers retornem o mesmo envelope e erros de validacao (Bean Validation) "
                "sejam traduzidos consistentemente."
            ),
            escopo=[
                "Envelope `{ success, data, error: { code, message, details } }` unificado.",
                "Handler para MethodArgumentNotValidException / ConstraintViolationException.",
                "Codigos de erro por dominio (AUTH_*, VINCULO_*, EMPRESTIMO_*, META_*).",
            ],
            aceite=[
                "Qualquer erro da API cai no handler global e retorna no formato padrao.",
                "Documentado no Swagger.",
            ],
        ),
    },

    # ==========================================================
    # AUTH (Dev A)
    # ==========================================================
    {
        "title": "feat(auth): entidade Usuario (pai) e migration revisadas",
        "labels": ["type:feature", "area:auth", "priority:P0", "track:A"],
        "body": body(
            epic="auth", track="A",
            contexto=(
                "Entidade `Usuario` ja existe no scaffold (usuario/entity). Revisar campos necessarios para pai: "
                "nome, email (unico), senha (hash), telefone, foto/avatar, provider (LOCAL/GOOGLE), "
                "created_at/updated_at, ativo."
            ),
            escopo=[
                "Revisar `Usuario.java` e `V3__create_usuarios.sql`.",
                "Garantir unicidade de email.",
                "Suportar usuarios vindos do OAuth2 (sem senha).",
            ],
            aceite=[
                "Migration idempotente, nao quebra historico.",
                "Testes unitarios do repositorio.",
            ],
        ),
    },
    {
        "title": "feat(auth): POST /auth/register - cadastro de pai",
        "labels": ["type:feature", "area:auth", "priority:P0", "track:A"],
        "body": body(
            epic="auth", track="A",
            contexto="Cadastro do pai/responsavel com email e senha.",
            escopo=[
                "POST /auth/register com nome, email, senha.",
                "Validacoes (email valido, senha forte, email unico).",
                "Hash de senha via PasswordEncoder ja existente.",
                "Retorna 201 + usuario basico (sem senha).",
            ],
            aceite=[
                "Email duplicado retorna 409 com codigo AUTH_EMAIL_JA_CADASTRADO.",
                "Senha nunca volta na resposta.",
                "Teste de integracao cobrindo sucesso e erro.",
            ],
            depende_de=["feat(auth): entidade Usuario (pai) e migration revisadas"],
        ),
    },
    {
        "title": "feat(auth): POST /auth/login - autenticacao JWT",
        "labels": ["type:feature", "area:auth", "priority:P0", "track:A"],
        "body": body(
            epic="auth", track="A",
            contexto="Autenticacao por email/senha retornando access token JWT e refresh token.",
            escopo=[
                "POST /auth/login com email + senha.",
                "Retorna access token (curta duracao) + refresh token.",
                "Persistencia de refresh token (tabela ja existe).",
            ],
            aceite=[
                "Credencial invalida retorna 401.",
                "Access token contem sub (user id) e expiracao.",
                "Teste de integracao cobrindo sucesso e credenciais invalidas.",
            ],
            depende_de=["feat(auth): POST /auth/register - cadastro de pai"],
        ),
    },
    {
        "title": "feat(auth): POST /auth/refresh - rotacao de refresh token",
        "labels": ["type:feature", "area:auth", "priority:P0", "track:A"],
        "body": body(
            epic="auth", track="A",
            contexto="Renovacao do access token via refresh token com rotacao.",
            escopo=[
                "POST /auth/refresh com refresh token.",
                "Invalida token antigo e emite par novo (rotacao).",
                "Revoga todos os refresh tokens do usuario em caso de reuso detectado.",
            ],
            aceite=[
                "Refresh token expirado/invalido retorna 401.",
                "Teste cobre rotacao e deteccao de reuso.",
            ],
            depende_de=["feat(auth): POST /auth/login - autenticacao JWT"],
        ),
    },
    {
        "title": "feat(auth): login com Google (OAuth2)",
        "labels": ["type:feature", "area:auth", "priority:P1", "track:A"],
        "body": body(
            epic="auth", track="A",
            contexto=(
                "Ja ha scaffold em `config/oauth2/` (CustomOidcUserService, OAuth2AuthenticationSuccessHandler). "
                "Finalizar fluxo para o mobile obter JWT apos login Google."
            ),
            escopo=[
                "Endpoint que recebe id_token Google do app e emite JWT/refresh.",
                "Criar `Usuario` com provider=GOOGLE se nao existir.",
                "Vincular a conta existente se email ja cadastrado (com aviso).",
            ],
            aceite=[
                "App mobile troca id_token Google por par JWT/refresh.",
                "Documentado no Swagger.",
            ],
            depende_de=["feat(auth): POST /auth/login - autenticacao JWT"],
            tecnicas=["Nao usar fluxo redirect-based (app mobile); trocar id_token direto."],
        ),
    },
    {
        "title": "feat(auth): recuperacao de senha (forgot + reset)",
        "labels": ["type:feature", "area:auth", "priority:P1", "track:A"],
        "body": body(
            epic="auth", track="A",
            contexto=(
                "Fluxo de esqueci-minha-senha: pai informa email, recebe token (por email), usa token para "
                "definir nova senha."
            ),
            escopo=[
                "POST /auth/forgot-password (email).",
                "POST /auth/reset-password (token + nova senha).",
                "Token com expiracao curta (ex. 30 min).",
                "Envio de email (pode ser log em dev).",
            ],
            aceite=[
                "Token usado nao pode ser reusado.",
                "Email inexistente responde 200 generico (anti-enumeracao).",
                "Teste cobre sucesso, token expirado e reuso.",
            ],
            depende_de=["feat(auth): POST /auth/login - autenticacao JWT"],
        ),
    },
    {
        "title": "feat(usuarios): GET e PUT /usuarios/me - perfil do pai",
        "labels": ["type:feature", "area:auth", "priority:P1", "track:A"],
        "body": body(
            epic="auth", track="A",
            contexto="Pai visualiza e edita o proprio perfil (nome, telefone, avatar).",
            escopo=[
                "GET /usuarios/me (usuario autenticado).",
                "PUT /usuarios/me (campos editaveis).",
                "Endpoint para alterar senha (requer senha atual).",
            ],
            aceite=[
                "Email nao e alteravel (ou requer fluxo proprio).",
                "Teste de integracao.",
            ],
            depende_de=["feat(auth): POST /auth/login - autenticacao JWT"],
        ),
    },

    # ==========================================================
    # VINCULO (Dev A apos auth core)
    # ==========================================================
    {
        "title": "feat(vinculo): POST /usuarios/me/alunos - vincular aluno",
        "labels": ["type:feature", "area:vinculo", "priority:P0", "track:A"],
        "body": body(
            epic="vinculo", track="A",
            contexto=(
                "Pai vincula um aluno ao proprio perfil informando matricula + um codigo/senha de validacao "
                "(combinado no modelo: pode ser codigo gerado pela escola). A tabela `usuario_aluno` ja existe."
            ),
            escopo=[
                "POST /usuarios/me/alunos com matricula + codigo de validacao.",
                "Valida que o aluno existe e que o codigo confere.",
                "Impede vinculo duplicado.",
            ],
            aceite=[
                "Retorna 201 com dados basicos do aluno vinculado.",
                "Codigo invalido -> 403 VINCULO_CODIGO_INVALIDO.",
                "Aluno inexistente -> 404.",
            ],
            depende_de=["feat(auth): POST /auth/login - autenticacao JWT"],
            tecnicas=[
                "Definir fonte do 'codigo de validacao' na issue (sugerido: campo novo em `alunos` populado pela escola ou gerado no momento do cadastro do aluno no backend legado).",
            ],
        ),
    },
    {
        "title": "feat(vinculo): GET /usuarios/me/alunos - listar filhos vinculados",
        "labels": ["type:feature", "area:vinculo", "priority:P0", "track:A"],
        "body": body(
            epic="vinculo", track="A",
            contexto="Lista alunos vinculados ao pai autenticado, com dados resumidos.",
            escopo=[
                "GET /usuarios/me/alunos.",
                "Retorna id, nome, matricula, turma (se relevante), qtdLivrosLidos, livroAtual (titulo).",
            ],
            aceite=[
                "So retorna alunos do pai logado.",
                "Teste cobrindo pai sem filhos (lista vazia).",
            ],
            depende_de=["feat(vinculo): POST /usuarios/me/alunos - vincular aluno"],
        ),
    },
    {
        "title": "feat(vinculo): DELETE /usuarios/me/alunos/{alunoId} - desvincular",
        "labels": ["type:feature", "area:vinculo", "priority:P2", "track:A"],
        "body": body(
            epic="vinculo", track="A",
            contexto="Pai remove o vinculo de um aluno (nao deleta o aluno, so o relacionamento).",
            escopo=[
                "DELETE /usuarios/me/alunos/{alunoId}.",
                "404 se nao existe vinculo.",
            ],
            aceite=[
                "Metas criadas pelo pai para esse aluno ficam inacessiveis (definir regra na issue).",
            ],
            depende_de=["feat(vinculo): POST /usuarios/me/alunos - vincular aluno"],
        ),
    },
    {
        "title": "feat(autorizacao): garantir que pai so acessa dados de alunos vinculados",
        "labels": ["type:feature", "area:vinculo", "priority:P0", "track:A"],
        "body": body(
            epic="vinculo", track="A",
            contexto=(
                "Regra transversal: em qualquer endpoint que receba alunoId ou emprestimoId ou metaId, conferir "
                "que o recurso pertence a um aluno vinculado ao pai autenticado."
            ),
            escopo=[
                "Service helper `AlunoVerificationService` (ja existe no scaffold) - usar/estender.",
                "Aplicar em controllers de alunos, emprestimos, metas.",
                "403 consistente via GlobalExceptionHandler.",
            ],
            aceite=[
                "Teste de integracao: pai X tenta acessar aluno de pai Y -> 403.",
                "Aplicado em todos os endpoints que acessam dados de aluno.",
            ],
            depende_de=[
                "feat(vinculo): POST /usuarios/me/alunos - vincular aluno",
                "chore(infra): padronizar ApiResponse, GlobalExceptionHandler e validacao",
            ],
        ),
    },

    # ==========================================================
    # LIVROS (Dev B, paralelo a tudo apos infra)
    # ==========================================================
    {
        "title": "feat(livros): entidade Livro completa + migration",
        "labels": ["type:feature", "area:livros", "priority:P1", "track:B"],
        "body": body(
            epic="livros", track="B",
            contexto=(
                "A entidade `Livro` do scaffold tem apenas id e titulo. Precisa de autor, sinopse, capa (url), "
                "categoria, isbn, total de paginas. A migration V5 do lottus-backend serve como referencia."
            ),
            escopo=[
                "Expandir `Livro.java`.",
                "Criar migration (proxima versao Flyway).",
                "Repository com busca por titulo/autor.",
            ],
            aceite=[
                "Migration aplica do zero e e idempotente.",
                "Teste do repository para busca.",
            ],
        ),
    },
    {
        "title": "feat(livros): GET /livros - listagem paginada com busca",
        "labels": ["type:feature", "area:livros", "priority:P1", "track:B"],
        "body": body(
            epic="livros", track="B",
            contexto="Listagem do catalogo para o pai escolher livro ao registrar nova leitura.",
            escopo=[
                "GET /livros?search=&categoria=&page=&size=.",
                "Paginacao padrao Spring.",
                "Ordenacao por titulo.",
            ],
            aceite=[
                "Busca case-insensitive por titulo/autor.",
                "Teste de integracao.",
            ],
            depende_de=["feat(livros): entidade Livro completa + migration"],
        ),
    },
    {
        "title": "feat(livros): GET /livros/{id} - detalhes",
        "labels": ["type:feature", "area:livros", "priority:P2", "track:B"],
        "body": body(
            epic="livros", track="B",
            contexto="Pagina de detalhes do livro no app.",
            escopo=[
                "GET /livros/{id}.",
                "Retorna todos os campos + categoria.",
            ],
            aceite=[
                "404 quando nao existe.",
            ],
            depende_de=["feat(livros): entidade Livro completa + migration"],
        ),
    },

    # ==========================================================
    # EMPRESTIMOS (Dev A apos vinculo)
    # ==========================================================
    {
        "title": "feat(emprestimos): entidade e migration revisadas",
        "labels": ["type:feature", "area:emprestimos", "priority:P0", "track:A"],
        "body": body(
            epic="emprestimos", track="A",
            contexto=(
                "Entidade `Emprestimo` ja existe com id, aluno, livro, dataEmprestimo, dataDevolucaoPrevista, "
                "diasAtrasados, statusEmprestimo. Avaliar se precisa: dataDevolucaoReal, observacoes, paginasLidas."
            ),
            escopo=[
                "Revisar `Emprestimo.java`.",
                "Criar migration para emprestimos no mobile (nao existe V6 no mobile, so no backend legado).",
                "Repository com consultas por aluno e por status.",
            ],
            aceite=[
                "Migration idempotente aplicada.",
                "Teste do repository (historico do aluno, leitura atual).",
            ],
            depende_de=["feat(livros): entidade Livro completa + migration"],
        ),
    },
    {
        "title": "feat(emprestimos): POST /alunos/{id}/emprestimos - cadastrar nova leitura",
        "labels": ["type:feature", "area:emprestimos", "priority:P0", "track:A"],
        "body": body(
            epic="emprestimos", track="A",
            contexto=(
                "Pai registra que o filho comecou a ler um livro. Cria emprestimo com status ATIVO para um livro "
                "existente (nao cria livro novo)."
            ),
            escopo=[
                "POST /alunos/{id}/emprestimos com livroId + dataDevolucaoPrevista.",
                "Valida autorizacao (filho vinculado).",
                "Impede criar novo emprestimo se ja existe um ATIVO para o mesmo aluno (regra a confirmar).",
            ],
            aceite=[
                "403 se aluno nao pertence ao pai.",
                "404 se livro nao existe.",
                "Teste de integracao.",
            ],
            depende_de=[
                "feat(emprestimos): entidade e migration revisadas",
                "feat(autorizacao): garantir que pai so acessa dados de alunos vinculados",
            ],
        ),
    },
    {
        "title": "feat(emprestimos): GET /alunos/{id}/emprestimos - historico",
        "labels": ["type:feature", "area:emprestimos", "priority:P0", "track:A"],
        "body": body(
            epic="emprestimos", track="A",
            contexto="Historico completo de leituras do aluno com filtros.",
            escopo=[
                "GET /alunos/{id}/emprestimos?status=&periodoInicio=&periodoFim=&page=&size=.",
                "Ordenacao por dataEmprestimo desc.",
                "Autorizacao por vinculo.",
            ],
            aceite=[
                "Paginado.",
                "Teste de integracao.",
            ],
            depende_de=[
                "feat(emprestimos): entidade e migration revisadas",
                "feat(autorizacao): garantir que pai so acessa dados de alunos vinculados",
            ],
        ),
    },
    {
        "title": "feat(emprestimos): GET /alunos/{id}/emprestimos/atual - livro em leitura",
        "labels": ["type:feature", "area:emprestimos", "priority:P1", "track:A"],
        "body": body(
            epic="emprestimos", track="A",
            contexto="Endpoint especifico para o card de 'livro atual' no dashboard.",
            escopo=[
                "GET /alunos/{id}/emprestimos/atual.",
                "Retorna emprestimo ATIVO mais recente ou 204.",
            ],
            aceite=[
                "204 quando aluno nao tem leitura ativa.",
                "Teste de integracao.",
            ],
            depende_de=["feat(emprestimos): entidade e migration revisadas"],
        ),
    },
    {
        "title": "feat(emprestimos): PATCH /emprestimos/{id}/concluir - concluir leitura",
        "labels": ["type:feature", "area:emprestimos", "priority:P0", "track:A"],
        "body": body(
            epic="emprestimos", track="A",
            contexto="Marca leitura como concluida (devolvida), atualiza qtdLivrosLidos do aluno e dispara hook de progresso de metas.",
            escopo=[
                "PATCH /emprestimos/{id}/concluir com dataDevolucaoReal (default hoje).",
                "Atualiza status para CONCLUIDO.",
                "Incrementa `aluno.qtdLivrosLidos`.",
                "Publica evento interno para metas.",
            ],
            aceite=[
                "Teste: apos concluir, qtdLivrosLidos do aluno aumenta.",
                "Teste: evento publicado (mock).",
                "Idempotencia: concluir duas vezes retorna 409.",
            ],
            depende_de=["feat(emprestimos): POST /alunos/{id}/emprestimos - cadastrar nova leitura"],
        ),
    },

    # ==========================================================
    # METAS (Dev B em paralelo)
    # ==========================================================
    {
        "title": "feat(metas): modelagem da entidade Meta + migration",
        "labels": ["type:feature", "area:metas", "priority:P0", "track:B"],
        "body": body(
            epic="metas", track="B",
            contexto=(
                "Entidade Meta: id, alunoId, criadoPorUsuarioId (pai), titulo, descricao (opcional), tipoMeta "
                "(QUANTIDADE_LIVROS, GENERO, PAGINAS, LIVRO_ESPECIFICO), tipoValidacao (BOOLEANA, PERCENTUAL), "
                "valorAlvo (nullable quando booleana), valorAtual, dataInicio, dataFim, status (ATIVA, CONCLUIDA, "
                "CANCELADA), created_at/updated_at."
            ),
            escopo=[
                "Entidade + enums.",
                "Migration Flyway.",
                "Repository.",
            ],
            aceite=[
                "Migration aplicada.",
                "Testes unitarios do repository.",
            ],
        ),
    },
    {
        "title": "feat(metas): POST /alunos/{id}/metas - criar meta",
        "labels": ["type:feature", "area:metas", "priority:P0", "track:B"],
        "body": body(
            epic="metas", track="B",
            contexto="Pai cria meta para filho vinculado. Informa tipo, titulo, duracao, forma de validacao.",
            escopo=[
                "POST /alunos/{id}/metas.",
                "Validar autorizacao por vinculo.",
                "Validar consistencia (BOOLEANA nao exige valorAlvo; PERCENTUAL exige >0).",
            ],
            aceite=[
                "403 para aluno nao vinculado.",
                "400 quando payload inconsistente.",
                "Teste de integracao.",
            ],
            depende_de=[
                "feat(metas): modelagem da entidade Meta + migration",
                "feat(autorizacao): garantir que pai so acessa dados de alunos vinculados",
            ],
        ),
    },
    {
        "title": "feat(metas): GET /alunos/{id}/metas - listar metas com progresso",
        "labels": ["type:feature", "area:metas", "priority:P0", "track:B"],
        "body": body(
            epic="metas", track="B",
            contexto="Lista metas de um aluno, retornando progresso (valorAtual/valorAlvo) e status.",
            escopo=[
                "GET /alunos/{id}/metas?status=.",
                "Retorna progresso percentual computado quando tipoValidacao=PERCENTUAL.",
            ],
            aceite=[
                "Aluno sem meta -> lista vazia.",
                "Autorizacao por vinculo aplicada.",
            ],
            depende_de=["feat(metas): POST /alunos/{id}/metas - criar meta"],
        ),
    },
    {
        "title": "feat(metas): PATCH /metas/{id} - atualizar progresso/concluir",
        "labels": ["type:feature", "area:metas", "priority:P1", "track:B"],
        "body": body(
            epic="metas", track="B",
            contexto="Pai atualiza progresso manualmente ou muda status.",
            escopo=[
                "PATCH /metas/{id} com valorAtual e/ou status.",
                "Se valorAtual>=valorAlvo e status!=CONCLUIDA, mover para CONCLUIDA automaticamente.",
            ],
            aceite=[
                "Autorizacao por vinculo.",
                "Teste da transicao automatica.",
            ],
            depende_de=["feat(metas): POST /alunos/{id}/metas - criar meta"],
        ),
    },
    {
        "title": "feat(metas): DELETE /metas/{id} - remover meta",
        "labels": ["type:feature", "area:metas", "priority:P2", "track:B"],
        "body": body(
            epic="metas", track="B",
            contexto="Pai pode remover meta (hard delete ou soft, a decidir na issue).",
            escopo=[
                "DELETE /metas/{id}.",
                "Autorizacao por vinculo.",
            ],
            aceite=[
                "404 quando nao existe.",
                "Teste de integracao.",
            ],
            depende_de=["feat(metas): POST /alunos/{id}/metas - criar meta"],
        ),
    },
    {
        "title": "feat(metas): atualizar progresso automaticamente em emprestimos",
        "labels": ["type:feature", "area:metas", "priority:P1", "track:B"],
        "body": body(
            epic="metas", track="B",
            contexto=(
                "Quando um emprestimo e concluido, metas ativas compativeis (tipo, periodo, filtros) do aluno "
                "devem ter valorAtual incrementado."
            ),
            escopo=[
                "Listener do evento de conclusao de emprestimo.",
                "Para tipoMeta=QUANTIDADE_LIVROS: +1 em metas ativas do aluno dentro do periodo.",
                "Para tipoMeta=GENERO: +1 se categoria do livro bate.",
                "Para tipoMeta=PAGINAS: soma paginas do livro.",
                "Para tipoMeta=LIVRO_ESPECIFICO: marca como concluida (booleana).",
            ],
            aceite=[
                "Teste ponta-a-ponta: criar meta, concluir emprestimo, progresso atualizado.",
                "Funciona com metas PERCENTUAL e BOOLEANA.",
            ],
            depende_de=[
                "feat(emprestimos): PATCH /emprestimos/{id}/concluir - concluir leitura",
                "feat(metas): PATCH /metas/{id} - atualizar progresso/concluir",
            ],
        ),
    },

    # ==========================================================
    # ALUNOS (dashboard)
    # ==========================================================
    {
        "title": "feat(alunos): GET /alunos/{id} - perfil do aluno",
        "labels": ["type:feature", "area:alunos", "priority:P1", "track:A"],
        "body": body(
            epic="alunos", track="A",
            contexto="Perfil do aluno na visao do pai (apenas leitura, sem dados de turma/escola sensiveis).",
            escopo=[
                "GET /alunos/{id}.",
                "Retorna id, nome, matricula, qtdLivrosLidos, qtdBonus (se fizer sentido), livroAtual.",
            ],
            aceite=[
                "Autorizacao por vinculo.",
                "Teste de integracao.",
            ],
            depende_de=["feat(autorizacao): garantir que pai so acessa dados de alunos vinculados"],
        ),
    },
    {
        "title": "feat(alunos): GET /alunos/{id}/dashboard - resumo",
        "labels": ["type:feature", "area:alunos", "priority:P1", "track:A"],
        "body": body(
            epic="alunos", track="A",
            contexto="Endpoint de dashboard otimizado para a tela inicial do app (evita multiplas chamadas).",
            escopo=[
                "GET /alunos/{id}/dashboard.",
                "Retorna: livroAtual, totalLivrosLidos, metasAtivas (resumo com progresso), ultimasLeituras (3).",
            ],
            aceite=[
                "Autorizacao por vinculo.",
                "Cache opcional (pode ser iteracao futura).",
            ],
            depende_de=[
                "feat(emprestimos): GET /alunos/{id}/emprestimos/atual - livro em leitura",
                "feat(metas): GET /alunos/{id}/metas - listar metas com progresso",
            ],
        ),
    },

    # ==========================================================
    # QUALIDADE
    # ==========================================================
    {
        "title": "test: testes unitarios de services (auth, vinculo, emprestimo, meta)",
        "labels": ["type:test", "area:infra", "priority:P1", "track:shared"],
        "body": body(
            epic="infra", track="shared",
            contexto="Cobertura minima de testes unitarios para services criticos.",
            escopo=[
                "AuthService (login, register, refresh).",
                "UsuarioAlunoService (vinculo).",
                "EmprestimoService (criar, concluir).",
                "MetaService (criar, atualizar progresso).",
            ],
            aceite=[
                ">= 70% de cobertura nas classes listadas.",
                "Testes rodam em CI.",
            ],
        ),
    },
    {
        "title": "test: testes de integracao de controllers principais",
        "labels": ["type:test", "area:infra", "priority:P1", "track:shared"],
        "body": body(
            epic="infra", track="shared",
            contexto="@SpringBootTest + Testcontainers (MySQL) para fluxos ponta-a-ponta.",
            escopo=[
                "AuthController (register/login/refresh).",
                "UsuarioAlunoController (vinculo).",
                "EmprestimoController (criar/listar/concluir).",
                "MetaController (criar/listar/atualizar).",
            ],
            aceite=[
                "Testes rodam em CI (cache de imagem docker).",
                "Cobrem autorizacao (pai X tentando acessar filho de Y).",
            ],
            depende_de=["chore(infra): configurar GitHub Actions (build + test)"],
        ),
    },
]

# ---------------------------------------------------------------------------
# Execucao
# ---------------------------------------------------------------------------
def run(cmd: list[str], capture: bool = True) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, capture_output=capture, text=True, encoding="utf-8")


def ensure_labels(dry_run: bool) -> None:
    existing = run(["gh", "label", "list", "-R", REPO, "--limit", "200",
                    "--json", "name"])
    if existing.returncode != 0:
        print("ERRO ao listar labels:", existing.stderr, file=sys.stderr)
        sys.exit(1)
    existing_names = {l["name"] for l in json.loads(existing.stdout or "[]")}
    for lbl in LABELS:
        name = lbl["name"]
        if name in existing_names:
            cmd = ["gh", "label", "edit", name, "-R", REPO,
                   "--color", lbl["color"], "--description", lbl["description"]]
            action = "update"
        else:
            cmd = ["gh", "label", "create", name, "-R", REPO,
                   "--color", lbl["color"], "--description", lbl["description"]]
            action = "create"
        print(f"[label:{action}] {name}")
        if dry_run:
            continue
        r = run(cmd)
        if r.returncode != 0:
            print(f"  ERRO: {r.stderr.strip()}", file=sys.stderr)


def existing_issue_titles() -> set[str]:
    r = run(["gh", "issue", "list", "-R", REPO, "--state", "all",
             "--limit", "500", "--json", "title"])
    if r.returncode != 0:
        print("ERRO ao listar issues:", r.stderr, file=sys.stderr)
        sys.exit(1)
    return {i["title"] for i in json.loads(r.stdout or "[]")}


def create_issues(dry_run: bool) -> None:
    already = existing_issue_titles()
    created, skipped = 0, 0
    for issue in ISSUES:
        title = issue["title"]
        if title in already:
            print(f"[issue:skip] ja existe: {title}")
            skipped += 1
            continue
        cmd = ["gh", "issue", "create", "-R", REPO,
               "--title", title, "--body", issue["body"]]
        for lbl in issue["labels"]:
            cmd += ["--label", lbl]
        print(f"[issue:create] {title}")
        if dry_run:
            created += 1
            continue
        r = run(cmd)
        if r.returncode != 0:
            print(f"  ERRO: {r.stderr.strip()}", file=sys.stderr)
        else:
            url = (r.stdout or "").strip().splitlines()[-1] if r.stdout else ""
            print(f"  -> {url}")
            created += 1
    print(f"\nResumo: {created} criadas, {skipped} ja existiam.")


def export_csv(path: Path) -> None:
    with path.open("w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, quoting=csv.QUOTE_ALL)
        w.writerow(["title", "labels", "body"])
        for issue in ISSUES:
            w.writerow([issue["title"], "|".join(issue["labels"]), issue["body"]])
    print(f"CSV escrito em {path}")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--only-labels", action="store_true")
    ap.add_argument("--export-csv", action="store_true")
    args = ap.parse_args()

    script_dir = Path(__file__).resolve().parent
    if args.export_csv:
        export_csv(script_dir / "issues.csv")

    ensure_labels(args.dry_run)
    if args.only_labels:
        return
    create_issues(args.dry_run)


if __name__ == "__main__":
    main()
