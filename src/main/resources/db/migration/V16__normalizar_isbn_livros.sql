-- V16: Normalizar ISBNs - remover hífens
-- Padroniza o formato dos ISBNs para conter apenas números

UPDATE livros
SET isbn = REPLACE(isbn, '-', '');

