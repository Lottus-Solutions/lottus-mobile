-- V14: Atualizar dados fictícios para período de janeiro a junho de 2026
-- Esta migration atualiza os empréstimos para o intervalo correto

-- Limpar empréstimos antigos
DELETE FROM emprestimos;

-- Reinserir com datas de janeiro a junho
INSERT INTO emprestimos (id, aluno_id, livro_id, data_emprestimo, data_devolucao_prevista, data_devolucao_efetiva, dias_atrasados, status_emprestimo) VALUES
(1, 1, 1, '2026-01-15', '2026-02-15', '2026-02-14', 0, 'FINALIZADO'),
(2, 2, 7, '2026-02-20', '2026-03-20', '2026-03-19', 0, 'FINALIZADO'),
(3, 3, 6, '2026-01-25', '2026-02-25', '2026-02-23', 0, 'FINALIZADO'),
(4, 4, 2, '2026-03-10', '2026-04-10', '2026-04-15', 5, 'FINALIZADO'),
(5, 5, 8, '2026-02-01', '2026-03-01', '2026-03-10', 9, 'FINALIZADO'),
(6, 6, 11, '2026-04-10', '2026-05-10', '2026-05-08', 0, 'FINALIZADO'),
(7, 7, 3, '2026-04-18', '2026-05-18', '2026-05-20', 2, 'FINALIZADO'),
(8, 8, 5, '2026-03-12', '2026-04-12', '2026-04-10', 0, 'FINALIZADO'),
(9, 9, 4, '2026-02-20', '2026-03-20', '2026-03-25', 5, 'FINALIZADO'),
(10, 10, 9, '2026-01-22', '2026-02-22', '2026-02-20', 0, 'FINALIZADO'),
(11, 11, 10, '2026-05-08', '2026-06-08', NULL, 1, 'ATRASADO'),
(12, 12, 12, '2026-05-16', '2026-06-16', NULL, 0, 'ATIVO'),
(13, 13, 1, '2026-05-28', '2026-06-28', NULL, 0, 'ATIVO'),
(14, 14, 2, '2026-05-20', '2026-06-20', NULL, 0, 'ATIVO'),
(15, 15, 7, '2026-06-01', '2026-07-01', NULL, 0, 'ATIVO'),
(16, 16, 11, '2026-05-15', '2026-06-15', NULL, 0, 'ATIVO'),
(17, 17, 6, '2026-06-03', '2026-07-03', NULL, 0, 'ATIVO'),
(18, 18, 3, '2026-05-25', '2026-06-25', NULL, 0, 'ATIVO'),
(19, 19, 5, '2026-05-18', '2026-06-18', NULL, 0, 'ATIVO'),
(20, 20, 4, '2026-06-05', '2026-07-05', NULL, 0, 'ATIVO');

