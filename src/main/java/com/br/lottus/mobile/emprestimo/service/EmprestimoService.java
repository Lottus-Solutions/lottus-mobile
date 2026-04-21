package com.br.lottus.mobile.emprestimo.service;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.aluno.repository.AlunoRepository;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.emprestimo.command.CreateEmprestimoCommand;
import com.br.lottus.mobile.emprestimo.command.EmprestimoResponse;
import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import com.br.lottus.mobile.emprestimo.entity.StatusEmprestimo;
import com.br.lottus.mobile.emprestimo.repository.EmprestimoRepository;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.repository.LivroRepository;
import com.br.lottus.mobile.meta.service.MetaService;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private static final int PRAZO_DEVOLUCAO_DIAS = 15;
    private static final List<StatusEmprestimo> STATUS_EM_LEITURA =
            List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO);

    private final EmprestimoRepository emprestimoRepository;
    private final AlunoRepository alunoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioAlunoRepository usuarioAlunoRepository;
    private final MetaService metaService;

    @Transactional
    public EmprestimoResponse registrarLeitura(Long usuarioId, String matricula, CreateEmprestimoCommand command) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);

        Livro livro = livroRepository.findById(command.livroId())
                .orElseThrow(() -> new BusinessException("Livro nao encontrado", HttpStatus.NOT_FOUND));

        emprestimoRepository
                .findFirstByAlunoIdAndStatusEmprestimoInOrderByDataEmprestimoDesc(aluno.getId(), STATUS_EM_LEITURA)
                .ifPresent(e -> {
                    throw new BusinessException(
                            "Aluno ja possui uma leitura em andamento",
                            HttpStatus.CONFLICT);
                });

        LocalDate dataEmprestimo = command.dataEmprestimo() != null ? command.dataEmprestimo() : LocalDate.now();
        LocalDate dataPrevista = command.dataDevolucaoPrevista() != null
                ? command.dataDevolucaoPrevista()
                : dataEmprestimo.plusDays(PRAZO_DEVOLUCAO_DIAS);

        if (dataPrevista.isBefore(dataEmprestimo)) {
            throw new BusinessException("Data de devolucao prevista nao pode ser anterior a data de inicio");
        }

        Emprestimo novo = Emprestimo.builder()
                .aluno(aluno)
                .livro(livro)
                .dataEmprestimo(dataEmprestimo)
                .dataDevolucaoPrevista(dataPrevista)
                .statusEmprestimo(calcularStatus(dataPrevista))
                .build();

        atualizarDiasAtrasados(novo);

        Emprestimo salvo = emprestimoRepository.save(novo);
        log.info("Leitura registrada id={} aluno={} livro={}", salvo.getId(), aluno.getMatricula(), livro.getId());

        return EmprestimoResponse.from(salvo);
    }

    @Transactional
    public EmprestimoResponse concluirLeitura(Long usuarioId, String matricula, Long emprestimoId) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);

        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new BusinessException("Leitura nao encontrada", HttpStatus.NOT_FOUND));

        if (emprestimo.getAluno() == null || !aluno.getId().equals(emprestimo.getAluno().getId())) {
            throw new BusinessException("Leitura nao pertence ao aluno informado", HttpStatus.FORBIDDEN);
        }

        if (emprestimo.getStatusEmprestimo() == StatusEmprestimo.FINALIZADO
                || emprestimo.getStatusEmprestimo() == StatusEmprestimo.ARQUIVADO) {
            throw new BusinessException("Leitura ja esta concluida", HttpStatus.CONFLICT);
        }

        emprestimo.setDataDevolucaoEfetiva(LocalDate.now());
        emprestimo.setStatusEmprestimo(StatusEmprestimo.FINALIZADO);
        atualizarDiasAtrasados(emprestimo);

        Emprestimo salvo = emprestimoRepository.save(emprestimo);
        log.info("Leitura concluida id={} aluno={}", salvo.getId(), aluno.getMatricula());

        metaService.registrarLeituraConcluida(aluno, salvo.getLivro(), salvo.getDataDevolucaoEfetiva());

        return EmprestimoResponse.from(salvo);
    }

    @Transactional(readOnly = true)
    public Optional<EmprestimoResponse> buscarLivroAtual(Long usuarioId, String matricula) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);

        return emprestimoRepository
                .findFirstByAlunoIdAndStatusEmprestimoInOrderByDataEmprestimoDesc(aluno.getId(), STATUS_EM_LEITURA)
                .map(e -> {
                    atualizarDiasAtrasados(e);
                    return EmprestimoResponse.from(e);
                });
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> buscarHistorico(Long usuarioId, String matricula) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);

        return emprestimoRepository.findByAlunoIdOrderByDataEmprestimoDesc(aluno.getId()).stream()
                .peek(this::atualizarDiasAtrasados)
                .map(EmprestimoResponse::from)
                .toList();
    }

    private Aluno carregarAlunoVinculado(Long usuarioId, String matricula) {
        Aluno aluno = alunoRepository.findByMatricula(matricula)
                .orElseThrow(() -> new BusinessException("Aluno nao encontrado", HttpStatus.NOT_FOUND));

        boolean vinculado = usuarioAlunoRepository.findByIdUsuarioId(usuarioId).stream()
                .anyMatch(ua -> ua.getAluno() != null && aluno.getId().equals(ua.getAluno().getId()));

        if (!vinculado) {
            throw new BusinessException("Aluno nao esta vinculado ao responsavel", HttpStatus.FORBIDDEN);
        }

        return aluno;
    }

    private StatusEmprestimo calcularStatus(LocalDate dataDevolucaoPrevista) {
        return LocalDate.now().isAfter(dataDevolucaoPrevista)
                ? StatusEmprestimo.ATRASADO
                : StatusEmprestimo.ATIVO;
    }

    private void atualizarDiasAtrasados(Emprestimo emprestimo) {
        if (emprestimo.getStatusEmprestimo() == StatusEmprestimo.FINALIZADO
                || emprestimo.getStatusEmprestimo() == StatusEmprestimo.ARQUIVADO) {
            LocalDate referencia = emprestimo.getDataDevolucaoEfetiva() != null
                    ? emprestimo.getDataDevolucaoEfetiva()
                    : LocalDate.now();
            long atraso = referencia.toEpochDay() - emprestimo.getDataDevolucaoPrevista().toEpochDay();
            emprestimo.setDiasAtrasados((int) Math.max(0, atraso));
            return;
        }

        LocalDate hoje = LocalDate.now();
        if (hoje.isAfter(emprestimo.getDataDevolucaoPrevista())) {
            long atraso = hoje.toEpochDay() - emprestimo.getDataDevolucaoPrevista().toEpochDay();
            emprestimo.setDiasAtrasados((int) atraso);
            emprestimo.setStatusEmprestimo(StatusEmprestimo.ATRASADO);
        } else {
            emprestimo.setDiasAtrasados(0);
            if (emprestimo.getStatusEmprestimo() == StatusEmprestimo.ATRASADO) {
                emprestimo.setStatusEmprestimo(StatusEmprestimo.ATIVO);
            }
        }
    }
}
