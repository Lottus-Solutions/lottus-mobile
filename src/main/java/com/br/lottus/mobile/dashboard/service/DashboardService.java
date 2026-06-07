package com.br.lottus.mobile.dashboard.service;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.aluno.repository.AlunoRepository;
import com.br.lottus.mobile.atividade.command.AtividadeResponse;
import com.br.lottus.mobile.atividade.repository.AtividadeRepository;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.dashboard.command.EvolucaoMensalResponse;
import com.br.lottus.mobile.dashboard.command.GeneroLeituraResponse;
import com.br.lottus.mobile.dashboard.command.ResumoDashboardResponse;
import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import com.br.lottus.mobile.emprestimo.entity.StatusEmprestimo;
import com.br.lottus.mobile.emprestimo.repository.EmprestimoRepository;
import com.br.lottus.mobile.meta.entity.Meta;
import com.br.lottus.mobile.meta.entity.StatusMeta;
import com.br.lottus.mobile.meta.repository.MetaRepository;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final List<StatusEmprestimo> STATUS_EM_LEITURA =
            List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO);

    private static final int MAX_MESES_EVOLUCAO = 24;
    private static final int DEFAULT_MESES_EVOLUCAO = 12;
    private static final int MAX_ATIVIDADES = 50;
    private static final int DEFAULT_ATIVIDADES = 20;

    private final EmprestimoRepository emprestimoRepository;
    private final MetaRepository metaRepository;
    private final AlunoRepository alunoRepository;
    private final UsuarioAlunoRepository usuarioAlunoRepository;
    private final AtividadeRepository atividadeRepository;

    @Transactional(readOnly = true)
    public ResumoDashboardResponse resumo(Long usuarioId, String matricula) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        LocalDate inicioMes = mesAtual.atDay(1);
        LocalDate fimMes = mesAtual.atEndOfMonth();

        long livrosLidosNoMes = emprestimoRepository.contarLidosNoPeriodo(aluno.getId(), inicioMes, fimMes);

        Optional<Emprestimo> leituraAtual = emprestimoRepository
                .findFirstByAlunoIdAndStatusEmprestimoInOrderByDataEmprestimoDesc(aluno.getId(), STATUS_EM_LEITURA);

        Integer diasComLivroAtual = leituraAtual
                .map(e -> (int) ChronoUnit.DAYS.between(e.getDataEmprestimo(), hoje))
                .map(d -> Math.max(d, 0))
                .orElse(null);
        String tituloLivroAtual = leituraAtual
                .map(e -> e.getLivro() != null ? e.getLivro().getTitulo() : null)
                .orElse(null);

        List<Meta> metasDoMes = metaRepository
                .findByAlunoIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
                        aluno.getId(), fimMes, inicioMes)
                .stream()
                .filter(m -> m.getStatus() != StatusMeta.ARQUIVADA)
                .toList();

        int percentualMedio = 0;
        int concluidas = 0;
        if (!metasDoMes.isEmpty()) {
            int soma = 0;
            for (Meta m : metasDoMes) {
                soma += m.percentual();
                if (m.getStatus() == StatusMeta.CONCLUIDA) {
                    concluidas++;
                }
            }
            percentualMedio = (int) Math.round((double) soma / metasDoMes.size());
        }

        return ResumoDashboardResponse.builder()
                .livrosLidosNoMes(livrosLidosNoMes)
                .diasComLivroAtual(diasComLivroAtual)
                .tituloLivroAtual(tituloLivroAtual)
                .percentualMetasMensais(percentualMedio)
                .totalMetasMensais(metasDoMes.size())
                .metasMensaisConcluidas(concluidas)
                .build();
    }

    @Transactional(readOnly = true)
    public List<GeneroLeituraResponse> generos(Long usuarioId, String matricula) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);

        List<Object[]> linhas = emprestimoRepository.contarLidosAgrupadoPorCategoria(aluno.getId());
        long totalGeral = linhas.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        if (totalGeral == 0) {
            return List.of();
        }

        List<GeneroLeituraResponse> resposta = new ArrayList<>(linhas.size());
        for (Object[] row : linhas) {
            String categoria = (String) row[0];
            long total = ((Number) row[1]).longValue();
            double percentual = Math.round((total * 10000.0) / totalGeral) / 100.0;
            resposta.add(GeneroLeituraResponse.builder()
                    .categoria(categoria)
                    .total(total)
                    .percentual(percentual)
                    .build());
        }
        return resposta;
    }

    @Transactional(readOnly = true)
    public List<EvolucaoMensalResponse> evolucaoMensal(Long usuarioId, String matricula, Integer mesesSolicitados) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);

        int meses = mesesSolicitados == null ? DEFAULT_MESES_EVOLUCAO : mesesSolicitados;
        if (meses < 1) meses = 1;
        if (meses > MAX_MESES_EVOLUCAO) meses = MAX_MESES_EVOLUCAO;

        YearMonth fimYm = YearMonth.now();
        YearMonth inicioYm = fimYm.minusMonths(meses - 1L);
        LocalDate inicio = inicioYm.atDay(1);
        LocalDate fim = fimYm.atEndOfMonth();

        Map<YearMonth, Long> totaisPorMes = new HashMap<>();
        for (Object[] row : emprestimoRepository.contarLidosAgrupadoPorMes(aluno.getId(), inicio, fim)) {
            int ano = ((Number) row[0]).intValue();
            int mes = ((Number) row[1]).intValue();
            long total = ((Number) row[2]).longValue();
            totaisPorMes.put(YearMonth.of(ano, mes), total);
        }

        List<EvolucaoMensalResponse> resultado = new ArrayList<>(meses);
        YearMonth cursor = inicioYm;
        while (!cursor.isAfter(fimYm)) {
            long total = totaisPorMes.getOrDefault(cursor, 0L);
            resultado.add(EvolucaoMensalResponse.builder()
                    .ano(cursor.getYear())
                    .mes(cursor.getMonthValue())
                    .total(total)
                    .build());
            cursor = cursor.plusMonths(1);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<AtividadeResponse> atividades(Long usuarioId, String matricula, Integer limitSolicitado) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);

        int limit = limitSolicitado == null ? DEFAULT_ATIVIDADES : limitSolicitado;
        if (limit < 1) limit = 1;
        if (limit > MAX_ATIVIDADES) limit = MAX_ATIVIDADES;

        return atividadeRepository
                .findByAlunoIdOrderByOcorridoEmDesc(aluno.getId(), PageRequest.of(0, limit))
                .stream()
                .map(AtividadeResponse::from)
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
}
