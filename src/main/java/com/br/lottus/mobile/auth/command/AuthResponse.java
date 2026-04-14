package com.br.lottus.mobile.auth.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Resposta de autenticacao contendo tokens JWT")
public record AuthResponse(

        @Schema(description = "Token de acesso JWT")
        String accessToken,

        @Schema(description = "Token de renovacao")
        String refreshToken,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "ID do usuario autenticado")
        Long userId,

        @Schema(description = "Nome do usuario autenticado")
        String nome
) {
}
