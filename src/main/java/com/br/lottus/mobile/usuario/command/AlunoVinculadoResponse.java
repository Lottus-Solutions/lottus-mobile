package com.br.lottus.mobile.usuario.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Resumo de um aluno vinculado ao responsavel")
public record AlunoVinculadoResponse(

        @Schema(description = "ID do aluno")
        Long id,

        @Schema(description = "Nome do aluno")
        String nome,

        @Schema(description = "Matricula (RA)")
        String matricula,

        @Schema(description = "Serie/turma")
        String turma,

        @Schema(description = "Quantidade de livros ja lidos")
        Integer qtdLivrosLidos,

        @Schema(description = "Titulo do livro em leitura no momento. Nulo se nao houver leitura em andamento")
        String livroAtual
) {
}
