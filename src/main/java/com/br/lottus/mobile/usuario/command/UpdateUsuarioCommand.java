package com.br.lottus.mobile.usuario.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualizacao do usuario")
public record UpdateUsuarioCommand(

        @Schema(description = "Novo nome do usuario", example = "Joao da Silva")
        @Size(min = 2, message = "Nome deve ter no minimo 2 caracteres")
        String nome,

        @Schema(description = "Novo ID do avatar", example = "avatar_03")
        String idAvatar
) {
}
