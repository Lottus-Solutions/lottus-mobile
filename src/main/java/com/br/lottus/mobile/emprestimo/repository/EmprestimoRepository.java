package com.br.lottus.mobile.emprestimo.repository;

import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import com.br.lottus.mobile.emprestimo.entity.StatusEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByAlunoId(Long alunoId);

    List<Emprestimo> findByAlunoIdOrderByDataEmprestimoDesc(Long alunoId);

    Optional<Emprestimo> findFirstByAlunoIdAndStatusEmprestimoInOrderByDataEmprestimoDesc(
            Long alunoId, List<StatusEmprestimo> status);
}
