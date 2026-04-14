package com.br.lottus.mobile.usuario.service;

import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.usuario.command.UpdateUsuarioCommand;
import com.br.lottus.mobile.usuario.command.UsuarioResponse;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.entity.UsuarioAluno;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import com.br.lottus.mobile.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAlunoRepository usuarioAlunoRepository;

    @Transactional(readOnly = true)
    public UsuarioResponse getProfile(Long userId) {
        Usuario usuario = findById(userId);
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse updateProfile(Long userId, UpdateUsuarioCommand command) {
        Usuario usuario = findById(userId);

        if (command.nome() != null && !command.nome().isBlank()) {
            usuario.setNome(command.nome());
        }
        if (command.idAvatar() != null) {
            usuario.setIdAvatar(command.idAvatar());
        }

        usuario = usuarioRepository.save(usuario);
        log.debug("Profile updated for userId: {}", userId);

        return toResponse(usuario);
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
                .dtRegistro(usuario.getDtRegistro())
                .idAvatar(usuario.getIdAvatar())
                .matriculasAlunos(matriculas)
                .build();
    }
}
