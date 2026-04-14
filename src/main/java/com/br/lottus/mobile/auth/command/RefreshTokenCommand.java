package com.br.lottus.mobile.auth.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisicao de renovacao de token")
public record RefreshTokenCommand(

        @Schema(description = "Refresh token atual")
        @NotBlank(message = "Refresh token e obrigatorio")
        String refreshToken
) {
}
