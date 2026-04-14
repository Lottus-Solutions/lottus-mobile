package com.br.lottus.mobile.auth.service;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.aluno.repository.AlunoRepository;
import com.br.lottus.mobile.auth.command.AuthResponse;
import com.br.lottus.mobile.auth.command.LoginCommand;
import com.br.lottus.mobile.auth.command.RegisterCommand;
import com.br.lottus.mobile.auth.entity.RefreshToken;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.config.security.JwtService;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.entity.UsuarioAluno;
import com.br.lottus.mobile.usuario.entity.UsuarioAlunoId;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import com.br.lottus.mobile.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final UsuarioAlunoRepository usuarioAlunoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterCommand command) {
        if (usuarioRepository.existsByEmail(command.email())) {
            throw new BusinessException("Email ja cadastrado", HttpStatus.CONFLICT);
        }

        Aluno aluno = alunoRepository.findByMatricula(command.matriculaAluno())
                .orElseThrow(() -> new BusinessException("Aluno com esta matricula nao encontrado", HttpStatus.NOT_FOUND));

        if (usuarioAlunoRepository.existsByIdAlunoId(aluno.getId())) {
            throw new BusinessException("Este aluno ja esta vinculado a outro responsavel", HttpStatus.CONFLICT);
        }

        Usuario usuario = Usuario.builder()
                .nome(command.nome())
                .email(command.email())
                .senha(passwordEncoder.encode(command.senha()))
                .build();
        usuario = usuarioRepository.save(usuario);

        UsuarioAluno vinculo = UsuarioAluno.builder()
                .id(new UsuarioAlunoId(usuario.getId(), aluno.getId()))
                .usuario(usuario)
                .aluno(aluno)
                .build();
        usuarioAlunoRepository.save(vinculo);

        log.info("New user registered: {}", usuario.getEmail());

        return buildAuthResponse(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginCommand command) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.email(), command.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", HttpStatus.NOT_FOUND));

        log.info("User logged in: {}", usuario.getEmail());

        return buildAuthResponse(usuario);
    }

    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.validateAndRotate(rawRefreshToken);
        Usuario usuario = refreshToken.getUsuario();

        log.debug("Token refreshed for user: {}", usuario.getEmail());

        return buildAuthResponse(usuario);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.revokeAllTokens(userId);
        log.info("User logged out, userId: {}", userId);
    }

    private AuthResponse buildAuthResponse(Usuario usuario) {
        String accessToken = jwtService.generateAccessToken(usuario, Map.of("userId", usuario.getId()));
        String refreshToken = refreshTokenService.createRefreshToken(usuario);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(usuario.getId())
                .nome(usuario.getNome())
                .build();
    }
}
