package com.br.lottus.mobile.emprestimo.repository;

import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByAlunoId(Long alunoId);
}
