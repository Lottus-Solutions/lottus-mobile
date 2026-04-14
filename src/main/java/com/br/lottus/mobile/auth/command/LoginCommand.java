package com.br.lottus.mobile.auth.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record LoginCommand(

        @Schema(description = "Email do usuario", example = "joao@email.com")
        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @Schema(description = "Senha do usuario", example = "senhaSegura123")
        @NotBlank(message = "Senha e obrigatoria")
        String senha
) {
}
