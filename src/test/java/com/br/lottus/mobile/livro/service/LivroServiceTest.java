package com.br.lottus.mobile.livro.service;

import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.livro.command.LivroResponse;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.repository.LivroRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository repository;

    @InjectMocks
    private LivroService livroService;

    private Livro livroExemplo;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        livroExemplo = Livro.builder()
                .id(1L)
                .titulo("Harry Potter")
                .autor("J.K. Rowling")
                .totalPaginas(300)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Testes de Listagem e Busca")
    class ListagemTests {

        @Test
        @DisplayName("Deve chamar busca filtrada quando o termo de busca for informado")
        void deveListarComFiltro() {
            // Arrange
            String busca = "Harry";
            Page<Livro> page = new PageImpl<>(List.of(livroExemplo));

            when(repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(eq(busca), eq(busca), any(Pageable.class)))
                    .thenReturn(page);

            // Act
            Page<LivroResponse> resultado = livroService.listarEBuscarPorTituloOuAutor(busca, pageable);

            // Assert
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).titulo()).isEqualTo("Harry Potter");
            verify(repository, times(1)).findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(anyString(), anyString(), any());
            verify(repository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve chamar findAll quando o termo de busca for nulo ou vazio")
        void deveListarTodosQuandoBuscaVazia() {
            // Arrange
            Page<Livro> page = new PageImpl<>(List.of(livroExemplo));
            when(repository.findAll(any(Pageable.class))).thenReturn(page);

            // Act
            Page<LivroResponse> resultado = livroService.listarEBuscarPorTituloOuAutor("", pageable);

            // Assert
            assertThat(resultado.getContent()).hasSize(1);
            verify(repository, times(1)).findAll(pageable);
            verify(repository, never()).findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca por ID")
    class BuscaPorIdTests {

        @Test
        @DisplayName("Deve retornar o livro quando o ID existir")
        void deveRetornarLivroPorId() {
            // Arrange
            when(repository.findById(1L)).thenReturn(Optional.of(livroExemplo));

            // Act
            Livro resultado = livroService.buscarPorId(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTitulo()).isEqualTo("Harry Potter");
        }

        @Test
        @DisplayName("Deve lançar BusinessException 404 quando o ID não existir")
        void deveLancarErroQuandoLivroNaoExistir() {
            // Arrange
            when(repository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> {
                livroService.buscarPorId(99L);
            });

            assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("Livro não encontrado");
        }
    }
}