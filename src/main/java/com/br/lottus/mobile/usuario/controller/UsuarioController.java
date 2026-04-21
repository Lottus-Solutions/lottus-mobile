package com.br.lottus.mobile.usuario.controller;

import com.br.lottus.mobile.common.entity.ApiResponse;
import com.br.lottus.mobile.usuario.command.ChangePasswordCommand;
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

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil", description = "Atualiza nome, telefone e/ou avatar. Email nao e alteravel por este endpoint")
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

    @PatchMapping("/me")
    @Operation(summary = "Atualizar perfil (parcial)", description = "Alias PATCH para atualizacao parcial do perfil")
    public ResponseEntity<ApiResponse<UsuarioResponse>> patchProfile(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody UpdateUsuarioCommand command) {
        return updateProfile(usuario, command);
    }

    @PostMapping("/me/senha")
    @Operation(summary = "Alterar senha", description = "Altera a senha do usuario autenticado. Requer a senha atual. Revoga todos os refresh tokens.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Nova senha igual a atual ou invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Senha atual incorreta")
    })
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody ChangePasswordCommand command) {
        usuarioService.changePassword(usuario.getId(), command);
        return ResponseEntity.noContent().build();
    }
}
