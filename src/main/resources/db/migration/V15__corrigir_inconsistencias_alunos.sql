-- V15: Corrigir inconsistências de dados entre alunos e empréstimos
-- Alinha qtd_livros_lidos com empréstimos finalizados reais
-- e garante consistência entre bonus e livros lidos

-- Passo 1: Resetar qtd_livros_lidos baseado em empréstimos finalizados
UPDATE alunos a
SET a.qtd_livros_lidos = (
    SELECT COUNT(*)
    FROM emprestimos e
    WHERE e.aluno_id = a.id
    AND e.status_emprestimo = 'FINALIZADO'
);

-- Passo 2: Ajustar bonus para ser proporcional às leituras
-- Cada livro lido = em média 30 a 60 de bonus
UPDATE alunos a
SET a.qtd_bonus = (
    a.qtd_livros_lidos * 25.0 + (RAND() * 50)
)
WHERE a.qtd_livros_lidos > 0;

-- Passo 3: Garantir que alunos sem livros lidos tenham bonus mínimo
UPDATE alunos
SET qtd_bonus = 0
WHERE qtd_livros_lidos = 0;

-- Verificação: mostrar alunos com dados agora consistentes
-- SELECT id, matricula, nome, qtd_livros_lidos, qtd_bonus FROM alunos;

