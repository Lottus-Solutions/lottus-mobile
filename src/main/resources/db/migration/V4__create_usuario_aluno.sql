CREATE TABLE usuario_aluno (
    usuario_id  BIGINT NOT NULL,
    aluno_id    BIGINT NOT NULL,

    PRIMARY KEY (usuario_id, aluno_id),
    INDEX idx_ua_aluno (aluno_id),

    CONSTRAINT fk_ua_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_ua_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
