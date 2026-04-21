package com.br.lottus.mobile.emprestimo.command;

import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import com.br.lottus.mobile.emprestimo.entity.StatusEmprestimo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "Representacao de uma leitura (emprestimo) do aluno")
public record EmprestimoResponse(

        @Schema(description = "Id do emprestimo")
        Long id,

        @Schema(description = "Matricula do aluno")
        String alunoMatricula,

        @Schema(description = "Nome do aluno")
        String alunoNome,

        @Schema(description = "Id do livro")
        Long livroId,

        @Schema(description = "Titulo do livro")
        String livroTitulo,

        @Schema(description = "Data em que a leitura comecou")
        LocalDate dataEmprestimo,

        @Schema(description = "Data prevista de devolucao")
        LocalDate dataDevolucaoPrevista,

        @Schema(description = "Data efetiva de devolucao/conclusao. Nulo quando ainda em leitura")
        LocalDate dataDevolucaoEfetiva,

        @Schema(description = "Dias em atraso na data atual")
        int diasAtrasados,

        @Schema(description = "Situacao da leitura")
        StatusEmprestimo status
) {

    public static EmprestimoResponse from(Emprestimo e) {
        return EmprestimoResponse.builder()
                .id(e.getId())
                .alunoMatricula(e.getAluno() != null ? e.getAluno().getMatricula() : null)
                .alunoNome(e.getAluno() != null ? e.getAluno().getNome() : null)
                .livroId(e.getLivro() != null ? e.getLivro().getId() : null)
                .livroTitulo(e.getLivro() != null ? e.getLivro().getTitulo() : null)
                .dataEmprestimo(e.getDataEmprestimo())
                .dataDevolucaoPrevista(e.getDataDevolucaoPrevista())
                .dataDevolucaoEfetiva(e.getDataDevolucaoEfetiva())
                .diasAtrasados(e.getDiasAtrasados())
                .status(e.getStatusEmprestimo())
                .build();
    }
}
