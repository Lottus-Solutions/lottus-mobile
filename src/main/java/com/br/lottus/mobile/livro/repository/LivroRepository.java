package com.br.lottus.mobile.livro.repository;

import com.br.lottus.mobile.livro.entity.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    Page<Livro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(
            String titulo, String autor, Pageable pageable);

}
