package com.br.lottus.mobile.usuario.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para alteracao de senha. Requer a senha atual")
public record ChangePasswordCommand(

        @NotBlank(message = "Senha atual obrigatoria")
        @Schema(description = "Senha atual do usuario")
        String senhaAtual,

        @NotBlank(message = "Nova senha obrigatoria")
        @Size(min = 8, message = "Nova senha deve ter no minimo 8 caracteres")
        @Schema(description = "Nova senha", example = "novaSenhaSegura123")
        String novaSenha
) {
}
