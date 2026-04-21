package com.br.lottus.mobile.meta.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Atualizacao manual de progresso de uma meta")
public record AtualizarProgressoCommand(

        @NotNull
        @Min(value = 0, message = "Valor deve ser maior ou igual a 0")
        @Schema(description = "Novo valor atual (absoluto). Para BOOLEAN use 0 ou 1")
        Integer valorAtual
) {
}
