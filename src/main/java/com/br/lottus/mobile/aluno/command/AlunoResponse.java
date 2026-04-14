package com.br.lottus.mobile.aluno.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Dados publicos do aluno para verificacao de RA")
public record AlunoResponse(

        @Schema(description = "Matricula (RA) do aluno")
        String matricula,

        @Schema(description = "Nome do aluno")
        String nome,

        @Schema(description = "Serie/turma do aluno")
        String serie,

        @Schema(description = "Indica se o aluno ja esta vinculado a um responsavel")
        boolean vinculado
) {
}
