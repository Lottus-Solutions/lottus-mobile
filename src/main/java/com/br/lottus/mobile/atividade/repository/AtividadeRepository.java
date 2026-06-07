package com.br.lottus.mobile.atividade.repository;

import com.br.lottus.mobile.atividade.entity.Atividade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    List<Atividade> findByAlunoIdOrderByOcorridoEmDesc(Long alunoId, Pageable pageable);
}
