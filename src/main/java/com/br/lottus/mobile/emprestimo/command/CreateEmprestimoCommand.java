package com.br.lottus.mobile.emprestimo.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Dados para registrar uma nova leitura (emprestimo) de um aluno vinculado")
public record CreateEmprestimoCommand(

        @NotNull
        @Schema(description = "Id do livro ja existente no acervo", example = "1")
        Long livroId,

        @Schema(description = "Data de inicio da leitura. Se nao informada, usa a data atual", example = "2026-04-21")
        LocalDate dataEmprestimo,

        @Schema(description = "Data prevista de devolucao. Se nao informada, assume 15 dias apos dataEmprestimo", example = "2026-05-06")
        LocalDate dataDevolucaoPrevista
) {
}
