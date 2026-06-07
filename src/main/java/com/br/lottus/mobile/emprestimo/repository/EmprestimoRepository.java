package com.br.lottus.mobile.emprestimo.repository;

import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import com.br.lottus.mobile.emprestimo.entity.StatusEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByAlunoId(Long alunoId);

    List<Emprestimo> findByAlunoIdOrderByDataEmprestimoDesc(Long alunoId);

    Optional<Emprestimo> findFirstByAlunoIdAndStatusEmprestimoInOrderByDataEmprestimoDesc(
            Long alunoId, List<StatusEmprestimo> status);

    @Query("""
            SELECT COUNT(e) FROM Emprestimo e
            WHERE e.aluno.id = :alunoId
              AND e.dataDevolucaoEfetiva IS NOT NULL
              AND e.dataDevolucaoEfetiva >= :inicio
              AND e.dataDevolucaoEfetiva <= :fim
            """)
    long contarLidosNoPeriodo(@Param("alunoId") Long alunoId,
                              @Param("inicio") LocalDate inicio,
                              @Param("fim") LocalDate fim);

    @Query("""
            SELECT YEAR(e.dataDevolucaoEfetiva) AS ano,
                   MONTH(e.dataDevolucaoEfetiva) AS mes,
                   COUNT(e) AS total
            FROM Emprestimo e
            WHERE e.aluno.id = :alunoId
              AND e.dataDevolucaoEfetiva IS NOT NULL
              AND e.dataDevolucaoEfetiva >= :inicio
              AND e.dataDevolucaoEfetiva <= :fim
            GROUP BY YEAR(e.dataDevolucaoEfetiva), MONTH(e.dataDevolucaoEfetiva)
            ORDER BY YEAR(e.dataDevolucaoEfetiva), MONTH(e.dataDevolucaoEfetiva)
            """)
    List<Object[]> contarLidosAgrupadoPorMes(@Param("alunoId") Long alunoId,
                                             @Param("inicio") LocalDate inicio,
                                             @Param("fim") LocalDate fim);

    @Query("""
            SELECT COALESCE(e.livro.categoria, 'SEM_CATEGORIA') AS categoria,
                   COUNT(e) AS total
            FROM Emprestimo e
            WHERE e.aluno.id = :alunoId
              AND e.dataDevolucaoEfetiva IS NOT NULL
            GROUP BY COALESCE(e.livro.categoria, 'SEM_CATEGORIA')
            ORDER BY COUNT(e) DESC
            """)
    List<Object[]> contarLidosAgrupadoPorCategoria(@Param("alunoId") Long alunoId);
}
