package com.br.lottus.mobile.usuario.repository;

import com.br.lottus.mobile.usuario.entity.UsuarioAluno;
import com.br.lottus.mobile.usuario.entity.UsuarioAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioAlunoRepository extends JpaRepository<UsuarioAluno, UsuarioAlunoId> {

    boolean existsByIdAlunoId(Long alunoId);

    List<UsuarioAluno> findByIdUsuarioId(Long usuarioId);

    List<UsuarioAluno> findByIdAlunoId(Long alunoId);
}
