package com.br.lottus.mobile.livro.controller;

import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.config.security.JwtAuthenticationFilter;
import com.br.lottus.mobile.config.security.JwtService;
import com.br.lottus.mobile.livro.command.LivroResponse;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.service.LivroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LivroController.class)

class LivroControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private LivroService livroService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Livro livroMock;
    private LivroResponse livroResponseMock;

    @BeforeEach
    void setUp() {
        // Setup robusto para evitar problemas com filtros de segurança
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        livroMock = Livro.builder()
                .id(1L)
                .titulo("O Hobbit")
                .autor("J.R.R. Tolkien")
                .build();

        livroResponseMock = LivroResponse.builder()
                .id(1L)
                .titulo("O Hobbit")
                .autor("J.R.R. Tolkien")
                .build();
    }

    @Test
    @DisplayName("GET /api/livros - Deve retornar lista paginada")
    void listarLivros_Sucesso() throws Exception {
        Page<LivroResponse> page = new PageImpl<>(List.of(livroResponseMock));

        when(livroService.listarEBuscarPorTituloOuAutor(any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/livros")
                        .with(user("user"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].titulo").value("O Hobbit"));
    }

    @Test
    @DisplayName("GET /api/livros?busca=Hobbit - Deve buscar por termo")
    void buscarLivros_Sucesso() throws Exception {
        Page<LivroResponse> page = new PageImpl<>(List.of(livroResponseMock));

        when(livroService.listarEBuscarPorTituloOuAutor(eq("Hobbit"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/livros")
                        .with(user("user"))
                        .param("busca", "Hobbit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].titulo").value("O Hobbit"));
    }

    @Test
    @DisplayName("GET /api/livros/{id} - Deve retornar um livro por ID")
    void buscarPorId_Sucesso() throws Exception {
        when(livroService.buscarPorId(1L)).thenReturn(livroMock);

        mockMvc.perform(get("/api/livros/{id}", 1L)
                        .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titulo").value("O Hobbit"));
    }

    @Test
    @DisplayName("GET /api/livros/{id} - Deve retornar 404 quando livro não existe")
    void buscarPorId_NotFound() throws Exception {
        when(livroService.buscarPorId(99L))
                .thenThrow(new BusinessException("Livro não encontrado", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/livros/{id}", 99L)
                        .with(user("user")))
                .andExpect(status().isNotFound());
    }
}