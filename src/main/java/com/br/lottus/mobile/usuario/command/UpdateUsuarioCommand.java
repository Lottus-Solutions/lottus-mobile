package com.br.lottus.mobile.usuario.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualizacao do perfil do usuario. Email nao e alteravel por este endpoint")
public record UpdateUsuarioCommand(

        @Schema(description = "Novo nome do usuario", example = "Joao da Silva")
        @Size(min = 2, message = "Nome deve ter no minimo 2 caracteres")
        String nome,

        @Schema(description = "Telefone de contato. Apenas digitos, ate 20 caracteres", example = "11987654321")
        @Pattern(regexp = "^[0-9+\\-()\\s]{8,20}$", message = "Telefone invalido")
        String telefone,

        @Schema(description = "Novo ID do avatar", example = "avatar_03")
        String idAvatar
) {
}
