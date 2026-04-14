CREATE TABLE livros (
    id      BIGINT          NOT NULL AUTO_INCREMENT,
    titulo  VARCHAR(255)    NOT NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
