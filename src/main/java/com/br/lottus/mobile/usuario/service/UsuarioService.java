package com.br.lottus.mobile.usuario.service;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.aluno.repository.AlunoRepository;
import com.br.lottus.mobile.auth.service.RefreshTokenService;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import com.br.lottus.mobile.emprestimo.entity.StatusEmprestimo;
import com.br.lottus.mobile.emprestimo.repository.EmprestimoRepository;
import com.br.lottus.mobile.usuario.command.AlunoVinculadoResponse;
import com.br.lottus.mobile.usuario.command.ChangePasswordCommand;
import com.br.lottus.mobile.usuario.command.UpdateUsuarioCommand;
import com.br.lottus.mobile.usuario.command.UsuarioResponse;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.entity.UsuarioAluno;
import com.br.lottus.mobile.usuario.entity.UsuarioAlunoId;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import com.br.lottus.mobile.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAlunoRepository usuarioAlunoRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AlunoRepository alunoRepository;

    private static final List<StatusEmprestimo> STATUS_EM_LEITURA =
            List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO);

    @Transactional(readOnly = true)
    public UsuarioResponse getProfile(Long userId) {
        Usuario usuario = findById(userId);
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<AlunoVinculadoResponse> listarAlunosVinculados(Long userId) {
        return usuarioAlunoRepository.findByIdUsuarioId(userId).stream()
                .map(UsuarioAluno::getAluno)
                .filter(Objects::nonNull)
                .map(this::toAlunoVinculadoResponse)
                .toList();
    }

    private AlunoVinculadoResponse toAlunoVinculadoResponse(Aluno aluno) {
        String livroAtual = emprestimoRepository
                .findFirstByAlunoIdAndStatusEmprestimoInOrderByDataEmprestimoDesc(aluno.getId(), STATUS_EM_LEITURA)
                .map(Emprestimo::getLivro)
                .map(livro -> livro != null ? livro.getTitulo() : null)
                .orElse(null);

        return AlunoVinculadoResponse.builder()
                .id(aluno.getId())
                .nome(aluno.getNome())
                .matricula(aluno.getMatricula())
                .turma(aluno.getTurma() != null ? aluno.getTurma().getSerie() : null)
                .qtdLivrosLidos(aluno.getQtdLivrosLidos())
                .livroAtual(livroAtual)
                .build();
    }

    @Transactional
    public UsuarioResponse updateProfile(Long userId, UpdateUsuarioCommand command) {
        Usuario usuario = findById(userId);

        if (command.nome() != null && !command.nome().isBlank()) {
            usuario.setNome(command.nome());
        }
        if (command.telefone() != null) {
            usuario.setTelefone(command.telefone().isBlank() ? null : command.telefone());
        }
        if (command.idAvatar() != null) {
            usuario.setIdAvatar(command.idAvatar());
        }

        usuario = usuarioRepository.save(usuario);
        log.debug("Profile updated for userId: {}", userId);

        return toResponse(usuario);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordCommand command) {
        Usuario usuario = findById(userId);

        if (!passwordEncoder.matches(command.senhaAtual(), usuario.getSenha())) {
            throw new BusinessException("Senha atual incorreta", HttpStatus.UNAUTHORIZED);
        }

        if (passwordEncoder.matches(command.novaSenha(), usuario.getSenha())) {
            throw new BusinessException("Nova senha deve ser diferente da atual");
        }

        usuario.setSenha(passwordEncoder.encode(command.novaSenha()));
        usuarioRepository.save(usuario);

        refreshTokenService.revokeAllTokens(userId);
        log.info("Password changed for userId: {}", userId);
    }

    private Usuario findById(Long userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", HttpStatus.NOT_FOUND));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        List<String> matriculas = usuarioAlunoRepository.findByIdUsuarioId(usuario.getId())
                .stream()
                .map(UsuarioAluno::getAluno)
                .map(aluno -> aluno.getMatricula())
                .toList();

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .telefone(usuario.getTelefone())
                .dtRegistro(usuario.getDtRegistro())
                .idAvatar(usuario.getIdAvatar())
                .matriculasAlunos(matriculas)
                .build();
    }

    @Transactional
    public Aluno vincularAluno(Long usuarioId, String matricula) {
        Aluno aluno = alunoRepository.findByMatricula(matricula)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado", HttpStatus.NOT_FOUND));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));

        UsuarioAlunoId idComposto = new UsuarioAlunoId(usuarioId, aluno.getId());
        if (usuarioAlunoRepository.existsById(idComposto)) {
            throw new BusinessException("Aluno já vinculado a este perfil", HttpStatus.CONFLICT);
        }

        UsuarioAluno novoVinculo = UsuarioAluno.builder()
                .id(idComposto)
                .usuario(usuario)
                .aluno(aluno)
                .build();

        usuarioAlunoRepository.save(novoVinculo);

        return aluno;
    }
}
