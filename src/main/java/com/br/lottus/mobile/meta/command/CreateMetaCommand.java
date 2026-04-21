package com.br.lottus.mobile.meta.command;

import com.br.lottus.mobile.meta.entity.TipoMeta;
import com.br.lottus.mobile.meta.entity.TipoValidacaoMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criar uma nova meta de leitura")
public record CreateMetaCommand(

        @NotNull
        @Schema(description = "Tipo da meta", example = "LIVROS_LIDOS")
        TipoMeta tipo,

        @NotBlank
        @Size(max = 150)
        @Schema(description = "Titulo da meta", example = "Ler 4 livros no mes")
        String titulo,

        @Size(max = 500)
        @Schema(description = "Descricao detalhada", example = "Incentivar leitura diaria")
        String descricao,

        @NotNull
        @Schema(description = "Forma de validacao", example = "PERCENTUAL")
        TipoValidacaoMeta tipoValidacao,

        @Min(value = 1, message = "Valor alvo deve ser maior ou igual a 1")
        @Schema(description = "Valor alvo. Para BOOLEAN deve ser 1", example = "4")
        Integer valorAlvo,

        @Schema(description = "Filtro para metas por palavra-chave no titulo do livro", example = "fantasia")
        String filtroValor,

        @NotNull
        @Schema(description = "Data de inicio da meta", example = "2026-04-01")
        LocalDate dataInicio,

        @NotNull
        @Schema(description = "Data de fim da meta", example = "2026-04-30")
        LocalDate dataFim
) {
}
