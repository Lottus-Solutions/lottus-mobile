package com.br.lottus.mobile.config.oauth2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Autenticacao", description = "Endpoints de registro, login, refresh e logout")
public class OAuth2DocumentationController {

    @GetMapping("/oauth2/authorization/google")
    @Operation(
            summary = "Login com Google (OAuth2/OIDC)",
            description = "Redireciona o usuario para a tela de login do Google. "
                    + "Apos autenticacao, o Google redireciona de volta para a aplicacao com os tokens JWT "
                    + "via query params no redirect URI configurado (ex: http://localhost:5173/oauth2/callback?accessToken=...&refreshToken=...). "
                    + "Este endpoint e gerenciado pelo Spring Security e nao deve ser chamado via AJAX/fetch.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redireciona para Google OAuth2"),
            @ApiResponse(responseCode = "500", description = "Erro na configuracao OAuth2")
    })
    public void googleLogin() {
    }
}
