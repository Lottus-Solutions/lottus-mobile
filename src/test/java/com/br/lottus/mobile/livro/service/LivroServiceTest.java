package com.br.lottus.mobile.livro.service;

import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.repository.LivroRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LivroServiceTest {

    @InjectMocks
    private LivroService livroService;

    @Mock
    private LivroRepository livroRepository;

    @Test
    void deveRetornarLivroQuandoIdExiste() {
        Long id = 1L;

        Livro livro = new Livro();
        livro.setId(id);
        livro.setTitulo("Clean Code");

        when(livroRepository.findById(id))
                .thenReturn(Optional.of(livro));

        Livro resultado = livroService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Clean Code", resultado.getTitulo());

        verify(livroRepository).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoExiste() {
        Long id = 99L;

        when(livroRepository.findById(id))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> livroService.buscarPorId(id)
        );

        assertEquals("Livro não encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

}
