package com.br.lottus.mobile.meta.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para atualizar uma meta existente (tipo e aluno nao podem mudar)")
public record UpdateMetaCommand(

        @Size(max = 150)
        @Schema(description = "Novo titulo")
        String titulo,

        @Size(max = 500)
        @Schema(description = "Nova descricao")
        String descricao,

        @Min(value = 1, message = "Valor alvo deve ser maior ou igual a 1")
        @Schema(description = "Novo valor alvo")
        Integer valorAlvo,

        @Schema(description = "Novo filtro (para metas por palavra-chave)")
        String filtroValor,

        @Schema(description = "Nova data de inicio")
        LocalDate dataInicio,

        @Schema(description = "Nova data de fim")
        LocalDate dataFim
) {
}
