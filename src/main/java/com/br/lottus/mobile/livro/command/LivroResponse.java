package com.br.lottus.mobile.livro.command;

import com.br.lottus.mobile.livro.entity.Livro;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Builder
@Schema(description = "Representacao de um livro")
public record LivroResponse(

        Long id,
        String titulo,
        String autor,
        String sinopse,
        String categoria,
        String isbn,
        Integer totalPaginas

) {

    public static LivroResponse from(Livro livro) {
        return LivroResponse.builder()
                .id(livro.getId())
                .titulo(livro.getTitulo())
                .autor(livro.getAutor())
                .sinopse(livro.getSinopse())
                .categoria(livro.getCategoria())
                .isbn(livro.getIsbn())
                .totalPaginas(livro.getTotalPaginas())
                .build();
    }
}