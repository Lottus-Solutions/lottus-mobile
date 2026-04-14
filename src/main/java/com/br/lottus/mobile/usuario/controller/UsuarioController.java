package com.br.lottus.mobile.usuario.controller;

import com.br.lottus.mobile.common.entity.ApiResponse;
import com.br.lottus.mobile.usuario.command.UpdateUsuarioCommand;
import com.br.lottus.mobile.usuario.command.UsuarioResponse;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gerenciamento do perfil do usuario (pai/responsavel)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil", description = "Retorna os dados do usuario autenticado com as matriculas dos alunos vinculados")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Nao autenticado")
    })
    public ResponseEntity<ApiResponse<UsuarioResponse>> getProfile(@AuthenticationPrincipal Usuario usuario) {
        UsuarioResponse response = usuarioService.getProfile(usuario.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/me")
    @Operation(summary = "Atualizar perfil", description = "Atualiza nome e/ou avatar do usuario autenticado")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Nao autenticado")
    })
    public ResponseEntity<ApiResponse<UsuarioResponse>> updateProfile(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody UpdateUsuarioCommand command) {
        UsuarioResponse response = usuarioService.updateProfile(usuario.getId(), command);
        return ResponseEntity.ok(ApiResponse.ok("Perfil atualizado com sucesso", response));
    }
}
