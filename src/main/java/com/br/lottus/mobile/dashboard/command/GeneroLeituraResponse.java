package com.br.lottus.mobile.dashboard.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Distribuicao de leituras concluidas agrupadas por categoria/genero")
public record GeneroLeituraResponse(

        @Schema(description = "Nome da categoria (ou SEM_CATEGORIA para livros sem categoria definida)")
        String categoria,

        @Schema(description = "Total de leituras concluidas nessa categoria")
        long total,

        @Schema(description = "Percentual da categoria sobre o total de leituras concluidas (0 a 100)")
        double percentual
) {
}
