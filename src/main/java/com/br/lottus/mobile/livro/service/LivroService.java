package com.br.lottus.mobile.livro.service;

import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.livro.command.LivroResponse;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.repository.LivroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LivroService {

    private static final Logger logger = LoggerFactory.getLogger(LivroService.class);

    private final LivroRepository repository;

    public LivroService(LivroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<LivroResponse> listarEBuscarPorTituloOuAutor(String busca, Pageable pageable) {
        Page<Livro> livros;

        if (busca != null && !busca.isBlank()) {
            livros = repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(
                    busca, busca, pageable);
        } else {
            livros = repository.findAll(pageable);
        }

        return livros.map(LivroResponse::from);
    }

    @Transactional(readOnly = true)
    public Livro buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Livro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Livro buscarPorIsbn(String isbn) {
        logger.info("Buscando livro com ISBN: {}", isbn);
        return Optional.ofNullable(repository.findByIsbn(isbn))
                .map(livro -> {
                    logger.info("Livro encontrado com ISBN: {} - Título: {}", isbn, livro);
                    return livro;
                })
                .orElseThrow(() -> {
                    logger.warn("Livro não encontrado com ISBN: {}", isbn);
                    return new BusinessException("Livro não encontrado", HttpStatus.NOT_FOUND);
                });
    }
}
