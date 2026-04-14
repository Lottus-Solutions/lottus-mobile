package com.br.lottus.mobile.auth.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registro de novo usuario (pai)")
public record RegisterCommand(

        @Schema(description = "Nome completo do usuario", example = "Joao Silva")
        @NotBlank(message = "Nome e obrigatorio")
        String nome,

        @Schema(description = "Email do usuario", example = "joao@email.com")
        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @Schema(description = "Senha do usuario", example = "senhaSegura123")
        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 6, message = "Senha deve ter no minimo 6 caracteres")
        String senha,

        @Schema(description = "Matricula (RA) do aluno a ser vinculado", example = "2024001")
        @NotBlank(message = "Matricula do aluno e obrigatoria")
        String matriculaAluno
) {
}
