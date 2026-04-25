package com.br.lottus.mobile.meta.service;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.aluno.repository.AlunoRepository;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.meta.command.AtualizarProgressoCommand;
import com.br.lottus.mobile.meta.command.CreateMetaCommand;
import com.br.lottus.mobile.meta.command.MetaResponse;
import com.br.lottus.mobile.meta.command.UpdateMetaCommand;
import com.br.lottus.mobile.meta.entity.Meta;
import com.br.lottus.mobile.meta.entity.StatusMeta;
import com.br.lottus.mobile.meta.entity.TipoMeta;
import com.br.lottus.mobile.meta.entity.TipoValidacaoMeta;
import com.br.lottus.mobile.meta.repository.MetaRepository;
import com.br.lottus.mobile.usuario.entity.Usuario;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import com.br.lottus.mobile.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;
    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAlunoRepository usuarioAlunoRepository;

    @Transactional
    public MetaResponse criar(Long usuarioId, String matricula, CreateMetaCommand cmd) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);
        Usuario criador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", HttpStatus.NOT_FOUND));

        validarJanela(cmd.dataInicio(), cmd.dataFim());
        int alvo = normalizarAlvo(cmd.tipoValidacao(), cmd.valorAlvo());
        validarFiltro(cmd.tipo(), cmd.filtroValor());

        Meta meta = Meta.builder()
                .aluno(aluno)
                .criadoPor(criador)
                .tipo(cmd.tipo())
                .titulo(cmd.titulo().trim())
                .descricao(cmd.descricao())
                .tipoValidacao(cmd.tipoValidacao())
                .valorAlvo(alvo)
                .valorAtual(0)
                .filtroValor(normalizarFiltro(cmd.filtroValor()))
                .dataInicio(cmd.dataInicio())
                .dataFim(cmd.dataFim())
                .status(StatusMeta.ATIVA)
                .build();

        Meta salva = metaRepository.save(meta);
        log.info("Meta criada id={} aluno={} tipo={}", salva.getId(), aluno.getMatricula(), salva.getTipo());
        return MetaResponse.from(salva);
    }

    @Transactional(readOnly = true)
    public List<MetaResponse> listar(Long usuarioId, String matricula, StatusMeta statusFiltro) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);

        List<Meta> metas = statusFiltro != null
                ? metaRepository.findByAlunoIdAndStatusOrderByDataInicioDesc(aluno.getId(), statusFiltro)
                : metaRepository.findByAlunoIdOrderByDataInicioDesc(aluno.getId());

        return metas.stream().map(MetaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MetaResponse buscar(Long usuarioId, String matricula, Long metaId) {
        Meta meta = carregarMetaDoAluno(usuarioId, matricula, metaId);
        return MetaResponse.from(meta);
    }

    @Transactional
    public MetaResponse atualizar(Long usuarioId, String matricula, Long metaId, UpdateMetaCommand cmd) {
        Meta meta = carregarMetaDoAluno(usuarioId, matricula, metaId);

        if (meta.getStatus() == StatusMeta.ARQUIVADA) {
            throw new BusinessException("Meta arquivada nao pode ser alterada", HttpStatus.CONFLICT);
        }

        if (cmd.titulo() != null && !cmd.titulo().isBlank()) {
            meta.setTitulo(cmd.titulo().trim());
        }
        if (cmd.descricao() != null) {
            meta.setDescricao(cmd.descricao());
        }
        if (cmd.valorAlvo() != null) {
            int alvo = normalizarAlvo(meta.getTipoValidacao(), cmd.valorAlvo());
            meta.setValorAlvo(alvo);
        }
        if (cmd.filtroValor() != null) {
            validarFiltro(meta.getTipo(), cmd.filtroValor());
            meta.setFiltroValor(normalizarFiltro(cmd.filtroValor()));
        }

        LocalDate novoInicio = cmd.dataInicio() != null ? cmd.dataInicio() : meta.getDataInicio();
        LocalDate novoFim = cmd.dataFim() != null ? cmd.dataFim() : meta.getDataFim();
        validarJanela(novoInicio, novoFim);
        meta.setDataInicio(novoInicio);
        meta.setDataFim(novoFim);

        revalidarStatus(meta);
        Meta salva = metaRepository.save(meta);
        return MetaResponse.from(salva);
    }

    @Transactional
    public MetaResponse atualizarProgressoManual(Long usuarioId, String matricula, Long metaId, AtualizarProgressoCommand cmd) {
        Meta meta = carregarMetaDoAluno(usuarioId, matricula, metaId);

        if (meta.getStatus() == StatusMeta.ARQUIVADA) {
            throw new BusinessException("Meta arquivada nao pode ter progresso alterado", HttpStatus.CONFLICT);
        }

        int valor = cmd.valorAtual();
        if (meta.getTipoValidacao() == TipoValidacaoMeta.BOOLEAN && valor > 1) {
            valor = 1;
        }
        if (valor > meta.getValorAlvo()) {
            valor = meta.getValorAlvo();
        }

        meta.setValorAtual(valor);
        revalidarStatus(meta);
        Meta salva = metaRepository.save(meta);
        log.info("Progresso manual atualizado meta={} valor={}", salva.getId(), salva.getValorAtual());
        return MetaResponse.from(salva);
    }

    @Transactional
    public void remover(Long usuarioId, String matricula, Long metaId) {
        Meta meta = carregarMetaDoAluno(usuarioId, matricula, metaId);
        metaRepository.delete(meta);
        log.info("Meta removida id={}", metaId);
    }

    @Transactional
    public void registrarLeituraConcluida(Aluno aluno, Livro livro, LocalDate dataConclusao) {
        LocalDate referencia = dataConclusao != null ? dataConclusao : LocalDate.now();

        List<Meta> candidatas = metaRepository
                .findByAlunoIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
                        aluno.getId(), StatusMeta.ATIVA, referencia, referencia);

        for (Meta meta : candidatas) {
            if (!encaixaNaRegra(meta, livro)) continue;

            int incremento = calcularIncremento(meta, livro);
            int novoValor = meta.getValorAtual() + incremento;
            if (meta.getTipoValidacao() == TipoValidacaoMeta.BOOLEAN) {
                novoValor = 1;
            }
            if (novoValor > meta.getValorAlvo()) {
                novoValor = meta.getValorAlvo();
            }
            meta.setValorAtual(novoValor);
            revalidarStatus(meta);
            metaRepository.save(meta);
            log.debug("Meta {} atualizada automaticamente para {}/{}", meta.getId(), meta.getValorAtual(), meta.getValorAlvo());
        }
    }

    private boolean encaixaNaRegra(Meta meta, Livro livro) {
        return switch (meta.getTipo()) {
            case LIVROS_LIDOS -> true;
            case LIVROS_COM_PALAVRA_CHAVE -> {
                if (meta.getFiltroValor() == null || livro == null || livro.getTitulo() == null) yield false;
                yield livro.getTitulo().toLowerCase().contains(meta.getFiltroValor().toLowerCase());
            }
            case GENERO -> {
                if (meta.getFiltroValor() == null || livro == null || livro.getCategoria() == null) yield false;
                yield livro.getCategoria().equalsIgnoreCase(meta.getFiltroValor());
            }
            case PAGINAS -> livro != null && livro.getTotalPaginas() != null && livro.getTotalPaginas() > 0;
            case LIVRO_ESPECIFICO -> {
                if (meta.getFiltroValor() == null || livro == null || livro.getId() == null) yield false;
                try {
                    yield Long.parseLong(meta.getFiltroValor().trim()) == livro.getId();
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case CUSTOM -> false;
        };
    }

    private int calcularIncremento(Meta meta, Livro livro) {
        if (meta.getTipo() == TipoMeta.PAGINAS && livro != null && livro.getTotalPaginas() != null) {
            return livro.getTotalPaginas();
        }
        return 1;
    }

    private void revalidarStatus(Meta meta) {
        if (meta.atingida() && meta.getStatus() != StatusMeta.CONCLUIDA) {
            meta.setStatus(StatusMeta.CONCLUIDA);
            meta.setConcluidaEm(LocalDateTime.now());
        } else if (!meta.atingida() && meta.getStatus() == StatusMeta.CONCLUIDA) {
            meta.setStatus(StatusMeta.ATIVA);
            meta.setConcluidaEm(null);
        }
    }

    private void validarJanela(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new BusinessException("Datas de inicio e fim sao obrigatorias");
        }
        if (fim.isBefore(inicio)) {
            throw new BusinessException("Data fim nao pode ser anterior a data de inicio");
        }
    }

    private int normalizarAlvo(TipoValidacaoMeta tipo, Integer alvoInformado) {
        if (tipo == TipoValidacaoMeta.BOOLEAN) {
            return 1;
        }
        if (alvoInformado == null || alvoInformado < 1) {
            throw new BusinessException("Valor alvo deve ser informado e maior ou igual a 1 para metas percentuais");
        }
        return alvoInformado;
    }

    private void validarFiltro(TipoMeta tipo, String filtro) {
        if (tipo == TipoMeta.LIVROS_COM_PALAVRA_CHAVE && (filtro == null || filtro.isBlank())) {
            throw new BusinessException("filtroValor obrigatorio para meta por palavra-chave");
        }
        if (tipo == TipoMeta.GENERO && (filtro == null || filtro.isBlank())) {
            throw new BusinessException("filtroValor obrigatorio para meta por genero (informe a categoria)");
        }
        if (tipo == TipoMeta.LIVRO_ESPECIFICO) {
            if (filtro == null || filtro.isBlank()) {
                throw new BusinessException("filtroValor obrigatorio para meta de livro especifico (informe o livroId)");
            }
            try {
                Long.parseLong(filtro.trim());
            } catch (NumberFormatException e) {
                throw new BusinessException("filtroValor de meta LIVRO_ESPECIFICO deve ser um livroId numerico");
            }
        }
    }

    private String normalizarFiltro(String filtro) {
        if (filtro == null) return null;
        String t = filtro.trim();
        return t.isEmpty() ? null : t;
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

    private Meta carregarMetaDoAluno(Long usuarioId, String matricula, Long metaId) {
        Aluno aluno = carregarAlunoVinculado(usuarioId, matricula);
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new BusinessException("Meta nao encontrada", HttpStatus.NOT_FOUND));

        if (meta.getAluno() == null || !aluno.getId().equals(meta.getAluno().getId())) {
            throw new BusinessException("Meta nao pertence ao aluno informado", HttpStatus.FORBIDDEN);
        }
        return meta;
    }
}
