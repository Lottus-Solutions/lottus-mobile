package com.br.lottus.mobile.dashboard.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Total de livros lidos em um determinado mes")
public record EvolucaoMensalResponse(

        @Schema(description = "Ano (ex.: 2026)")
        int ano,

        @Schema(description = "Mes (1 a 12)")
        int mes,

        @Schema(description = "Total de leituras concluidas no mes")
        long total
) {
}
