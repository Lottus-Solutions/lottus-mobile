package com.br.lottus.mobile.aluno.service;

import com.br.lottus.mobile.aluno.command.AlunoResponse;
import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.aluno.repository.AlunoRepository;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlunoVerificationService {

    private final AlunoRepository alunoRepository;
    private final UsuarioAlunoRepository usuarioAlunoRepository;

    @Transactional(readOnly = true)
    public AlunoResponse verificarMatricula(String matricula) {
        Aluno aluno = alunoRepository.findByMatricula(matricula)
                .orElseThrow(() -> new BusinessException("Aluno nao encontrado com esta matricula", HttpStatus.NOT_FOUND));

        boolean vinculado = usuarioAlunoRepository.existsByIdAlunoId(aluno.getId());

        log.debug("RA verification for {}: found={}, vinculado={}", matricula, true, vinculado);

        return AlunoResponse.builder()
                .matricula(aluno.getMatricula())
                .nome(aluno.getNome())
                .serie(aluno.getTurma() != null ? aluno.getTurma().getSerie() : null)
                .vinculado(vinculado)
                .build();
    }
}
