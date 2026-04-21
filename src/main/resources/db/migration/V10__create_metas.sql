CREATE TABLE metas (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    aluno_id                BIGINT          NOT NULL,
    criado_por_usuario_id   BIGINT          NOT NULL,
    tipo                    VARCHAR(40)     NOT NULL,
    titulo                  VARCHAR(150)    NOT NULL,
    descricao               VARCHAR(500)    NULL,
    tipo_validacao          VARCHAR(20)     NOT NULL,
    valor_alvo              INT             NOT NULL DEFAULT 1,
    valor_atual             INT             NOT NULL DEFAULT 0,
    filtro_valor            VARCHAR(150)    NULL,
    data_inicio             DATE            NOT NULL,
    data_fim                DATE            NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ATIVA',
    concluida_em            TIMESTAMP       NULL,
    criada_em               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizada_em           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_meta_aluno (aluno_id),
    INDEX idx_meta_criador (criado_por_usuario_id),
    INDEX idx_meta_status (status),
    INDEX idx_meta_janela (data_inicio, data_fim),

    CONSTRAINT fk_meta_aluno   FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE,
    CONSTRAINT fk_meta_usuario FOREIGN KEY (criado_por_usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
