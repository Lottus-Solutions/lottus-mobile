package com.br.lottus.mobile.livro.service;

import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.repository.LivroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LivroService {

    private final LivroRepository repository;

    public LivroService(LivroRepository repository) {
        this.repository = repository;
    }

    public Livro buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Livro não encontrado", HttpStatus.NOT_FOUND));
    }

}
