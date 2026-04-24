package com.br.lottus.mobile.livro.controller;

import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.common.exception.GlobalExceptionHandler;
import com.br.lottus.mobile.config.TestSecurityConfig;
import com.br.lottus.mobile.config.security.JwtAuthenticationFilter;
import com.br.lottus.mobile.config.security.JwtService;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.service.LivroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LivroController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class})
public class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LivroService livroService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveRetornarLivroQuandoIdExiste() throws Exception {
        Long id = 1L;

        Livro livro = new Livro(
                id,
                "Clean Code",
                "Robert C. Martin",
                "Livro sobre boas práticas",
                "Programação",
                "",
                0
        );

        when(livroService.buscarPorId(id)).thenReturn(livro);

        mockMvc.perform(get("/api/livros/{id}", id))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.titulo").value("Clean Code"))
                .andExpect(jsonPath("$.data.autor").value("Robert C. Martin"))
                .andExpect(jsonPath("$.data.categoria").value("Programação"));
    }

    @Test
    void deveRetornar404QuandoLivroNaoExiste() throws Exception {
        Long id = 99L;

        when(livroService.buscarPorId(id))
                .thenThrow(new BusinessException("Livro não encontrado", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/livros/{id}", id))
                .andExpect(status().isNotFound());
    }

}
