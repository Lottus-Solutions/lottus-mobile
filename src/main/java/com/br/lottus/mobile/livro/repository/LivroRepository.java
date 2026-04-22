package com.br.lottus.mobile.livro.repository;

import com.br.lottus.mobile.livro.entity.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    @Query("""
        SELECT l FROM Livro l
        WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
           OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :termo, '%'))
    """)
    List<Livro> buscarPorTituloOuAutor(@Param("termo") String termo);

}
