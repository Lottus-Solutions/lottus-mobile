package com.br.lottus.mobile.auth.controller;

import com.br.lottus.mobile.auth.command.AuthResponse;
import com.br.lottus.mobile.auth.command.LoginCommand;
import com.br.lottus.mobile.auth.command.RefreshTokenCommand;
import com.br.lottus.mobile.auth.command.RegisterCommand;
import com.br.lottus.mobile.auth.service.AuthService;
import com.br.lottus.mobile.common.entity.ApiResponse;
import com.br.lottus.mobile.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Endpoints de registro, login, refresh e logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuario (pai)", description = "Cria um novo usuario vinculado a um aluno existente pela matricula (RA)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Aluno nao encontrado com a matricula informada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email ja cadastrado ou aluno ja vinculado")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterCommand command) {
        AuthResponse response = authService.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Usuario registrado com sucesso", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica o usuario e retorna tokens JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Credenciais invalidas")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginCommand command) {
        AuthResponse response = authService.login(command);
        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Gera novos tokens usando o refresh token (rotacao automatica)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token renovado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token invalido ou expirado")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenCommand command) {
        AuthResponse response = authService.refreshToken(command.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Token renovado com sucesso", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga todos os refresh tokens do usuario autenticado")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout realizado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Usuario nao autenticado")
    })
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Usuario usuario) {
        authService.logout(usuario.getId());
        return ResponseEntity.ok(ApiResponse.ok("Logout realizado com sucesso"));
    }
}
