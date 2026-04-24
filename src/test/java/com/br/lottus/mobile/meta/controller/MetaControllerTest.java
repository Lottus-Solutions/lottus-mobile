package com.br.lottus.mobile.meta.controller;

import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.config.security.JwtAuthenticationFilter;
import com.br.lottus.mobile.config.security.JwtService;
import com.br.lottus.mobile.meta.command.CreateMetaCommand;
import com.br.lottus.mobile.meta.command.MetaResponse;
import com.br.lottus.mobile.meta.entity.StatusMeta;
import com.br.lottus.mobile.meta.entity.TipoMeta;
import com.br.lottus.mobile.meta.entity.TipoValidacaoMeta;
import com.br.lottus.mobile.meta.service.MetaService;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetaController.class)
class MetaControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MetaService metaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Usuario usuarioMock;
    private MetaResponse metaResponseMock;
    private final String MATRICULA = "MAT123";

    @BeforeEach
    void setUp() {
        // Configuração que resolveu o seu problema de segurança
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        usuarioMock = Usuario.builder()
                .id(1L)
                .nome("Responsavel")
                .email("pai@email.com")
                .build();

        metaResponseMock = MetaResponse.builder()
                .id(100L)
                .titulo("Ler Harry Potter")
                .status(StatusMeta.ATIVA)
                .valorAlvo(1)
                .valorAtual(0)
                .build();
    }

    @Test
    @DisplayName("POST /api/alunos/{matricula}/metas - Sucesso")
    void criarMeta_Sucesso() throws Exception {
        CreateMetaCommand command = new CreateMetaCommand(
                TipoMeta.LIVROS_LIDOS, "Ler Harry Potter", "Desc",
                TipoValidacaoMeta.BOOLEAN, 1, null,
                LocalDate.now(), LocalDate.now().plusDays(30)
        );

        when(metaService.criar(eq(1L), eq(MATRICULA), any())).thenReturn(metaResponseMock);

        mockMvc.perform(post("/api/alunos/{matricula}/metas", MATRICULA)
                        .with(user(usuarioMock)) // Agora funciona perfeitamente!
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.titulo").value("Ler Harry Potter"));
    }

    @Test
    @DisplayName("POST /api/alunos/{matricula}/metas - 403 Aluno não vinculado")
    void criarMeta_403() throws Exception {
        CreateMetaCommand command = new CreateMetaCommand(TipoMeta.LIVROS_LIDOS, "T", "D", TipoValidacaoMeta.BOOLEAN, 1, null, LocalDate.now(), LocalDate.now());

        when(metaService.criar(anyLong(), anyString(), any()))
                .thenThrow(new BusinessException("Aluno nao esta vinculado", HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/api/alunos/{matricula}/metas", MATRICULA)
                        .with(user(usuarioMock))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/alunos/{matricula}/metas - 400 Payload inconsistente")
    void criarMeta_400() throws Exception {
        CreateMetaCommand command = new CreateMetaCommand(TipoMeta.LIVROS_LIDOS, "T", "D", TipoValidacaoMeta.PERCENTUAL, -1, null, LocalDate.now(), LocalDate.now());

        when(metaService.criar(anyLong(), anyString(), any()))
                .thenThrow(new BusinessException("Valor alvo inválido", HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/alunos/{matricula}/metas", MATRICULA)
                        .with(user(usuarioMock))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/alunos/{matricula}/metas/{id} - 204")
    void removerMeta_Sucesso() throws Exception {
        mockMvc.perform(delete("/api/alunos/{matricula}/metas/{metaId}", MATRICULA, 100L)
                        .with(user(usuarioMock))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}