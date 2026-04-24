package com.br.lottus.mobile.usuario.service;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.aluno.repository.AlunoRepository;
import com.br.lottus.mobile.auth.service.RefreshTokenService;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import com.br.lottus.mobile.emprestimo.repository.EmprestimoRepository;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.meta.entity.StatusMeta;
import com.br.lottus.mobile.meta.repository.MetaRepository;
import com.br.lottus.mobile.usuario.command.AlunoVinculadoResponse;
import com.br.lottus.mobile.usuario.command.ChangePasswordCommand;
import com.br.lottus.mobile.usuario.command.UpdateUsuarioCommand;
import com.br.lottus.mobile.usuario.command.UsuarioResponse;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.entity.UsuarioAluno;
import com.br.lottus.mobile.usuario.entity.UsuarioAlunoId;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import com.br.lottus.mobile.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioAlunoRepository usuarioAlunoRepository;
    @Mock
    private EmprestimoRepository emprestimoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private MetaRepository metaRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Long userId = 1L;
    private Long alunoId = 2L;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(userId)
                .nome("Usuario Teste")
                .email("teste@email.com")
                .senha("encoded_password")
                .build();
    }

    @Nested
    @DisplayName("Testes de Perfil (Profile)")
    class ProfileTests {

        @Test
        @DisplayName("Deve retornar o perfil do usuário com sucesso")
        void getProfile_Success() {
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(usuarioAlunoRepository.findByIdUsuarioId(userId)).thenReturn(List.of());

            UsuarioResponse response = usuarioService.getProfile(userId);

            assertNotNull(response);
            assertEquals(usuario.getNome(), response.nome());
            verify(usuarioRepository).findById(userId);
        }

        @Test
        @DisplayName("Deve lançar 404 quando usuário não existir")
        void getProfile_NotFound() {
            when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () -> usuarioService.getProfile(userId));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }

        @Test
        @DisplayName("Deve atualizar o perfil parcialmente")
        void updateProfile_PartialUpdate() {
            UpdateUsuarioCommand command = new UpdateUsuarioCommand("Novo Nome", null, "avatar_v2");
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArguments()[0]);

            UsuarioResponse response = usuarioService.updateProfile(userId, command);

            assertEquals("Novo Nome", response.nome());
            assertEquals("avatar_v2", response.idAvatar());
            verify(usuarioRepository).save(usuario);
        }
    }

    @Nested
    @DisplayName("Testes de Alunos e Vínculos")
    class VinculoTests {

        @Test
        @DisplayName("Deve listar alunos vinculados e buscar o livro atual")
        void listarAlunosVinculados_Success() {
            Aluno aluno = Aluno.builder().id(10L).nome("João").matricula("M1").build();
            UsuarioAluno vinculo = UsuarioAluno.builder().aluno(aluno).build();

            Livro livro = Livro.builder().titulo("O Pequeno Príncipe").build();
            Emprestimo emprestimo = Emprestimo.builder().livro(livro).build();

            when(usuarioAlunoRepository.findByIdUsuarioId(userId)).thenReturn(List.of(vinculo));
            when(emprestimoRepository.findFirstByAlunoIdAndStatusEmprestimoInOrderByDataEmprestimoDesc(any(), any()))
                    .thenReturn(Optional.of(emprestimo));

            List<AlunoVinculadoResponse> result = usuarioService.listarAlunosVinculados(userId);

            assertFalse(result.isEmpty());
            assertEquals("O Pequeno Príncipe", result.get(0).livroAtual());
            assertEquals("João", result.get(0).nome());
        }

        @Test
        @DisplayName("Deve vincular um novo aluno com sucesso")
        void vincularAluno_Success() {
            String matricula = "2024ABC";
            Aluno aluno = Aluno.builder().id(50L).matricula(matricula).build();

            when(alunoRepository.findByMatricula(matricula)).thenReturn(Optional.of(aluno));
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(usuarioAlunoRepository.existsById(any(UsuarioAlunoId.class))).thenReturn(false);

            Aluno result = usuarioService.vincularAluno(userId, matricula);

            assertNotNull(result);
            verify(usuarioAlunoRepository).save(any(UsuarioAluno.class));
        }

        @Test
        @DisplayName("Deve lançar CONFLICT ao tentar vincular aluno já vinculado")
        void vincularAluno_Duplicate() {
            String matricula = "2024ABC";
            Aluno aluno = Aluno.builder().id(50L).matricula(matricula).build();

            when(alunoRepository.findByMatricula(matricula)).thenReturn(Optional.of(aluno));
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(usuarioAlunoRepository.existsById(any(UsuarioAlunoId.class))).thenReturn(true);

            assertThrows(BusinessException.class, () -> usuarioService.vincularAluno(userId, matricula));
        }
    }

    @Nested
    @DisplayName("Testes de Senha")
    class PasswordTests {

        @Test
        @DisplayName("Deve trocar a senha com sucesso e revogar tokens")
        void changePassword_Success() {
            ChangePasswordCommand command = new ChangePasswordCommand("senhaAtual", "novaSenha");

            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senhaAtual", usuario.getSenha())).thenReturn(true);
            when(passwordEncoder.matches("novaSenha", usuario.getSenha())).thenReturn(false);
            when(passwordEncoder.encode("novaSenha")).thenReturn("new_encoded_password");

            usuarioService.changePassword(userId, command);

            verify(usuarioRepository).save(usuario);
            verify(refreshTokenService).revokeAllTokens(userId);
            assertEquals("new_encoded_password", usuario.getSenha());
        }

        @Test
        @DisplayName("Deve lançar erro se a senha atual estiver incorreta")
        void changePassword_WrongCurrentPassword() {
            ChangePasswordCommand command = new ChangePasswordCommand("errada", "nova");

            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("errada", usuario.getSenha())).thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class, () -> usuarioService.changePassword(userId, command));
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }

        @Test
        @DisplayName("Deve lançar erro se a nova senha for igual à atual")
        void changePassword_SamePassword() {
            ChangePasswordCommand command = new ChangePasswordCommand("senha", "senha");

            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senha", usuario.getSenha())).thenReturn(true); // para a atual
            // matches da nova senha também retornará true (indicando que é igual)

            assertThrows(BusinessException.class, () -> usuarioService.changePassword(userId, command));
        }
    }

    @Nested
    @DisplayName("Testes de Desvínculo de Aluno")
    class DesvinculoTests {

        @Test
        @DisplayName("Deve desvincular aluno e arquivar metas com sucesso")
        void deveDesvincularAlunoComSucesso() {
            // Arrange
            Long usuarioId = 1L;
            Long alunoId = 2L;

            // Simula que o vínculo foi encontrado
            UsuarioAluno vinculoFake = new UsuarioAluno();
            when(usuarioAlunoRepository.findById(any(UsuarioAlunoId.class)))
                    .thenReturn(Optional.of(vinculoFake));

            // Act
            usuarioService.desvincularAluno(usuarioId, alunoId);

            // Assert
            verify(metaRepository).arquivarMetasPaiAluno(eq(usuarioId), eq(alunoId), any(), any());
            verify(usuarioAlunoRepository).deleteById(any(UsuarioAlunoId.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando o vínculo não existe")
        void deveLancarExcecaoQuandoVinculoNaoExiste() {
            // Arrange
            when(usuarioAlunoRepository.findById(any(UsuarioAlunoId.class)))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(BusinessException.class, () -> {
                usuarioService.desvincularAluno(1L, 2L);
            });

            // Mockito não reclamará de "unnecessary stubbing" aqui
            // pois não definimos nenhum when() para o metaRepository neste cenário
        }
    }
}
