package com.br.lottus.mobile.usuario;

import com.br.lottus.mobile.usuario.command.ChangePasswordCommand;
import com.br.lottus.mobile.usuario.command.UpdateUsuarioCommand;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Usuario usuario;
    private final String senhaOriginal = "senhaAtual123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        String email = "teste-" + System.nanoTime() + "@example.com";
        usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuario Teste")
                .email(email)
                .senha(passwordEncoder.encode(senhaOriginal))
                .build());
    }

    @Test
    void getMe_retornaPerfilDoUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/api/usuarios/me").with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(usuario.getId()))
                .andExpect(jsonPath("$.data.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.data.nome").value("Usuario Teste"));
    }

    @Test
    void getMe_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putMe_atualizaNomeTelefoneEAvatar() throws Exception {
        UpdateUsuarioCommand command = new UpdateUsuarioCommand("Novo Nome", "11987654321", "avatar_05");

        mockMvc.perform(put("/api/usuarios/me")
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.data.telefone").value("11987654321"))
                .andExpect(jsonPath("$.data.idAvatar").value("avatar_05"));

        Usuario atualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(atualizado.getNome()).isEqualTo("Novo Nome");
        assertThat(atualizado.getTelefone()).isEqualTo("11987654321");
        assertThat(atualizado.getIdAvatar()).isEqualTo("avatar_05");
    }

    @Test
    void putMe_naoAlteraEmail() throws Exception {
        String emailOriginal = usuario.getEmail();
        UpdateUsuarioCommand command = new UpdateUsuarioCommand("Outro Nome", null, null);

        mockMvc.perform(put("/api/usuarios/me")
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(emailOriginal));

        Usuario atualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(atualizado.getEmail()).isEqualTo(emailOriginal);
    }

    @Test
    void putMe_telefoneInvalido_retorna400() throws Exception {
        UpdateUsuarioCommand command = new UpdateUsuarioCommand(null, "abc", null);

        mockMvc.perform(put("/api/usuarios/me")
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alterarSenha_comSenhaAtualCorreta_retorna204() throws Exception {
        ChangePasswordCommand command = new ChangePasswordCommand(senhaOriginal, "novaSenhaSegura123");

        mockMvc.perform(post("/api/usuarios/me/senha")
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNoContent());

        Usuario atualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("novaSenhaSegura123", atualizado.getSenha())).isTrue();
        assertThat(passwordEncoder.matches(senhaOriginal, atualizado.getSenha())).isFalse();
    }

    @Test
    void alterarSenha_comSenhaAtualIncorreta_retorna401() throws Exception {
        ChangePasswordCommand command = new ChangePasswordCommand("senhaErrada", "novaSenhaSegura123");

        mockMvc.perform(post("/api/usuarios/me/senha")
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void alterarSenha_novaIgualAAtual_retorna400() throws Exception {
        ChangePasswordCommand command = new ChangePasswordCommand(senhaOriginal, senhaOriginal);

        mockMvc.perform(post("/api/usuarios/me/senha")
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alterarSenha_novaMuitoCurta_retorna400() throws Exception {
        ChangePasswordCommand command = new ChangePasswordCommand(senhaOriginal, "curta");

        mockMvc.perform(post("/api/usuarios/me/senha")
                        .with(user(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }
}
