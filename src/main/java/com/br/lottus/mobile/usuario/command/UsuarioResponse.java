package com.br.lottus.mobile.usuario.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Dados do usuario (pai)")
public record UsuarioResponse(

        @Schema(description = "ID do usuario")
        Long id,

        @Schema(description = "Nome completo")
        String nome,

        @Schema(description = "Email")
        String email,

        @Schema(description = "Data de registro")
        LocalDateTime dtRegistro,

        @Schema(description = "ID do avatar")
        String idAvatar,

        @Schema(description = "Matriculas dos alunos vinculados")
        List<String> matriculasAlunos
) {
}
