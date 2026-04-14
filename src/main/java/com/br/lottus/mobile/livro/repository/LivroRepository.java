package com.br.lottus.mobile.livro.repository;

import com.br.lottus.mobile.livro.entity.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {
}
