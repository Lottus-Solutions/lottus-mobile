CREATE TABLE alunos (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    matricula       VARCHAR(50)     NOT NULL,
    nome            VARCHAR(255)    NOT NULL,
    qtd_bonus       DOUBLE          NOT NULL DEFAULT 0,
    qtd_livros_lidos INTEGER        NOT NULL DEFAULT 0,
    turma_id        BIGINT          NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_aluno_matricula (matricula),
    INDEX idx_aluno_turma (turma_id),

    CONSTRAINT fk_aluno_turma FOREIGN KEY (turma_id) REFERENCES turmas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
