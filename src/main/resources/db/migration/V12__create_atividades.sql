CREATE TABLE atividades (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    aluno_id        BIGINT          NOT NULL,
    usuario_id      BIGINT          NOT NULL,
    tipo            VARCHAR(40)     NOT NULL,
    referencia_tipo VARCHAR(20)     NOT NULL,
    referencia_id   BIGINT          NOT NULL,
    titulo_resumo   VARCHAR(255)    NOT NULL,
    ocorrido_em     TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    INDEX idx_atividades_aluno_data (aluno_id, ocorrido_em),
    INDEX idx_atividades_usuario (usuario_id),

    CONSTRAINT fk_atividades_aluno   FOREIGN KEY (aluno_id)   REFERENCES alunos(id)   ON DELETE CASCADE,
    CONSTRAINT fk_atividades_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
