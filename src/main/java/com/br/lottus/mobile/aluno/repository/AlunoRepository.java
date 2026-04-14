package com.br.lottus.mobile.aluno.repository;

import com.br.lottus.mobile.aluno.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);
}
