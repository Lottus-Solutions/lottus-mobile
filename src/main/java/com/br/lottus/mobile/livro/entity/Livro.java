package com.br.lottus.mobile.livro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "livros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(nullable = false, length = 2000)
    private String sinopse;

    private String categoria;

    @Column(unique = true)
    private String isbn;

    private Integer totalPaginas;
}
