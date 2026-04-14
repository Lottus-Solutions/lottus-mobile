CREATE TABLE emprestimos (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    aluno_id                BIGINT          NOT NULL,
    livro_id                BIGINT          NOT NULL,
    data_emprestimo         DATE            NOT NULL,
    data_devolucao_prevista DATE            NOT NULL,
    dias_atrasados          INT             NOT NULL DEFAULT 0,
    status_emprestimo       VARCHAR(20)     NOT NULL DEFAULT 'ATIVO',

    PRIMARY KEY (id),
    INDEX idx_emp_aluno (aluno_id),
    INDEX idx_emp_livro (livro_id),
    INDEX idx_emp_status (status_emprestimo),

    CONSTRAINT fk_emp_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_livro FOREIGN KEY (livro_id) REFERENCES livros(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
