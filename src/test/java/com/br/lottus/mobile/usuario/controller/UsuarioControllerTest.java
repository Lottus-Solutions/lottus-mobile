package com.br.lottus.mobile.usuario.controller;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.config.security.JwtAuthenticationFilter;
import com.br.lottus.mobile.config.security.JwtService;
import com.br.lottus.mobile.usuario.command.ChangePasswordCommand;
import com.br.lottus.mobile.usuario.command.UpdateUsuarioCommand;
import com.br.lottus.mobile.usuario.command.UsuarioResponse;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.service.UsuarioService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        usuarioMock = Usuario.builder()
                .id(1L)
                .nome("Joao Silva")
                .email("joao@email.com")
                .senha("encoded_password")
                .build();
    }

    @Test
    @DisplayName("GET /api/usuarios/me - Deve retornar perfil")
    void getProfile_Success() throws Exception {
        UsuarioResponse response = UsuarioResponse.builder().nome("Joao Silva").build();
        when(usuarioService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/usuarios/me")
                        .with(user(usuarioMock))) // Simula o @AuthenticationPrincipal
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Joao Silva"));
    }

    @Test
    @DisplayName("POST /api/usuarios/me/senha - Deve retornar 204")
    void changePassword_Success() throws Exception {
        ChangePasswordCommand command = new ChangePasswordCommand("senhaAtual123", "novaSenha123");

        mockMvc.perform(post("/api/usuarios/me/senha")
                        .with(user(usuarioMock))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/usuarios/me/alunos - Deve retornar lista")
    void listarAlunosVinculados_Success() throws Exception {
        when(usuarioService.listarAlunosVinculados(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios/me/alunos")
                        .with(user(usuarioMock)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/usuarios/me - Deve atualizar perfil")
    void updateProfile_Success() throws Exception {
        UpdateUsuarioCommand command = new UpdateUsuarioCommand("Novo Nome", "11999998888", "avatar1");
        UsuarioResponse response = UsuarioResponse.builder().nome("Novo Nome").build();

        when(usuarioService.updateProfile(eq(1L), any(UpdateUsuarioCommand.class))).thenReturn(response);

        mockMvc.perform(put("/api/usuarios/me")
                        .with(user(usuarioMock))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Novo Nome"));
    }

    @Test
    @DisplayName("PATCH /api/usuarios/me - Deve atualizar perfil parcialmente")
    void patchProfile_Success() throws Exception {
        UpdateUsuarioCommand command = new UpdateUsuarioCommand("Nome Patch", null, null);
        UsuarioResponse response = UsuarioResponse.builder().nome("Nome Patch").build();

        when(usuarioService.updateProfile(eq(1L), any(UpdateUsuarioCommand.class))).thenReturn(response);

        mockMvc.perform(patch("/api/usuarios/me")
                        .with(user(usuarioMock))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Nome Patch"));
    }

    @Test
    @DisplayName("POST /api/usuarios/me/alunos/{matricula} - Deve vincular aluno")
    void vincularAluno_Success() throws Exception {
        String matricula = "MAT12345";
        Aluno alunoEntity = Aluno.builder()
                .id(10L)
                .nome("Aluno Teste")
                .matricula(matricula)
                .build();

        when(usuarioService.vincularAluno(eq(1L), eq(matricula))).thenReturn(alunoEntity);

        mockMvc.perform(post("/api/usuarios/me/alunos/{matricula}", matricula)
                        .with(user(usuarioMock))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.matricula").value(matricula))
                .andExpect(jsonPath("$.data.vinculado").value(true));
    }

    @Test
    @DisplayName("Deve retornar 204 quando o desvínculo for bem sucedido")
    void deveRetornar204AoDesvincularComSucesso() throws Exception {
        Long alunoId = 1L;

        doNothing().when(usuarioService).desvincularAluno(anyLong(), anyLong());

        mockMvc.perform(delete("/api/usuarios/me/alunos/{alunoId}", alunoId)
                        .with(user(usuarioMock))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 quando o vínculo não existir")
    void deveRetornar404QuandoVinculoNaoEncontrado() throws Exception {
        Long alunoId = 99L;

        doThrow(new BusinessException("Vínculo não encontrado", HttpStatus.NOT_FOUND))
                .when(usuarioService).desvincularAluno(anyLong(), anyLong());

        mockMvc.perform(delete("/api/usuarios/me/alunos/{alunoId}", alunoId)
                        .with(user(usuarioMock))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
