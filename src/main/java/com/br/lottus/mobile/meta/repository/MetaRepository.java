package com.br.lottus.mobile.meta.repository;

import com.br.lottus.mobile.meta.entity.Meta;
import com.br.lottus.mobile.meta.entity.StatusMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MetaRepository extends JpaRepository<Meta, Long> {

    List<Meta> findByAlunoIdOrderByDataInicioDesc(Long alunoId);

    List<Meta> findByAlunoIdAndStatusOrderByDataInicioDesc(Long alunoId, StatusMeta status);

    List<Meta> findByAlunoIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
            Long alunoId, StatusMeta status, LocalDate inicio, LocalDate fim);

    @Modifying
    @Query("UPDATE Meta m SET m.status = :statusArquivada " +
            "WHERE m.criadoPor.id = :usuarioId " +
            "AND m.aluno.id = :alunoId " +
            "AND m.status != :statusConcluida")
    void arquivarMetasPaiAluno(
            Long usuarioId,
            Long alunoId,
            StatusMeta statusArquivada,
            StatusMeta statusConcluida
    );
}
