CREATE TABLE usuarios (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nome            VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    senha           VARCHAR(255)    NOT NULL,
    dt_registro     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_avatar       VARCHAR(100)    NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
