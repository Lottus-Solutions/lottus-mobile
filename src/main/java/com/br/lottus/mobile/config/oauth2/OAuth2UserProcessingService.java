package com.br.lottus.mobile.config.oauth2;

import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario process(OAuth2UserInfo userInfo) {
        return usuarioRepository.findByEmail(userInfo.getEmail())
                .map(existing -> updateExistingUser(existing, userInfo))
                .orElseGet(() -> createNewUser(userInfo));
    }

    private Usuario updateExistingUser(Usuario usuario, OAuth2UserInfo userInfo) {
        log.debug("Updating existing OAuth2 user: {}", userInfo.getEmail());
        usuario.setNome(userInfo.getName());
        if (userInfo.getAvatarUrl() != null) {
            usuario.setIdAvatar(userInfo.getAvatarUrl());
        }
        return usuarioRepository.save(usuario);
    }

    private Usuario createNewUser(OAuth2UserInfo userInfo) {
        log.info("Creating new OAuth2 user: {}", userInfo.getEmail());
        Usuario usuario = Usuario.builder()
                .nome(userInfo.getName())
                .email(userInfo.getEmail())
                .senha(passwordEncoder.encode(UUID.randomUUID().toString()))
                .idAvatar(userInfo.getAvatarUrl())
                .build();
        return usuarioRepository.save(usuario);
    }
}
