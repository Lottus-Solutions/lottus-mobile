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
import com.br.lottus.mobile.usuario.entity.UsuarioAluno;
import com.br.lottus.mobile.usuario.repository.UsuarioAlunoRepository;
import com.br.lottus.mobile.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaServiceTest {

    @Mock private MetaRepository metaRepository;
    @Mock private AlunoRepository alunoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioAlunoRepository usuarioAlunoRepository;

    @InjectMocks
    private MetaService metaService;

    private final Long USER_ID = 1L;
    private final String MATRICULA = "MAT123";
    private final Long ALUNO_ID = 10L;

    private Aluno alunoMock;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        alunoMock = Aluno.builder().id(ALUNO_ID).matricula(MATRICULA).nome("Aluno Teste").build();
        usuarioMock = Usuario.builder().id(USER_ID).nome("Pai").build();
    }

    private void mockVinculoSucesso() {
        when(alunoRepository.findByMatricula(MATRICULA)).thenReturn(Optional.of(alunoMock));
        UsuarioAluno vinculo = new UsuarioAluno();
        vinculo.setAluno(alunoMock);
        when(usuarioAlunoRepository.findByIdUsuarioId(USER_ID)).thenReturn(List.of(vinculo));
    }

    @Nested
    @DisplayName("Testes de Criação de Meta")
    class CriarMetaTests {

        @Test
        @DisplayName("Deve criar meta booleana com sucesso (alvo forçado para 1)")
        void criarMetaBooleanSucesso() {
            mockVinculoSucesso();
            when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuarioMock));

            CreateMetaCommand cmd = new CreateMetaCommand(
                    TipoMeta.LIVROS_LIDOS,"Ler um livro", "Desc",
                    TipoValidacaoMeta.BOOLEAN, 99,
                    null, LocalDate.now(), LocalDate.now().plusDays(7)
            );

            when(metaRepository.save(ArgumentMatchers.<Meta>any())).thenAnswer(invocation -> {
                Meta m = invocation.getArgument(0);
                m.setId(100L);
                return m;
            });

            MetaResponse resp = metaService.criar(USER_ID, MATRICULA, cmd);

            assertThat(resp.valorAlvo()).isEqualTo(1);
            assertThat(resp.titulo()).isEqualTo("Ler um livro");
            verify(metaRepository).save(ArgumentMatchers.<Meta>any());
        }

        @Test
        @DisplayName("Deve lançar 403 quando o aluno não está vinculado ao pai")
        void criarMetaSemVinculoErro() {
            when(alunoRepository.findByMatricula(MATRICULA)).thenReturn(Optional.of(alunoMock));
            when(usuarioAlunoRepository.findByIdUsuarioId(USER_ID)).thenReturn(List.of()); // Lista vazia

            CreateMetaCommand cmd = new CreateMetaCommand(TipoMeta.LIVROS_LIDOS,
                    "Titulo",
                    "Desc",
                    TipoValidacaoMeta.BOOLEAN,
                    1,
                    null,
                    LocalDate.now(),
                    LocalDate.now()
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> metaService.criar(USER_ID, MATRICULA, cmd));
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Deve lançar erro quando meta por palavra-chave não tem filtro")
        void criarMetaPalavraChaveSemFiltroErro() {
            mockVinculoSucesso();
            when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuarioMock));

            CreateMetaCommand cmd = new CreateMetaCommand(
                    TipoMeta.LIVROS_COM_PALAVRA_CHAVE,
                    "Ler Harry",
                    "Desc",
                    TipoValidacaoMeta.PERCENTUAL,
                    5,
                    "",
                    LocalDate.now(),
                    LocalDate.now().plusDays(1)
            );

            assertThrows(BusinessException.class, () -> metaService.criar(USER_ID, MATRICULA, cmd));
        }
    }

    @Nested
    @DisplayName("Testes de Atualização e Progresso")
    class AtualizarMetaTests {

        @Test
        @DisplayName("Não deve permitir atualizar meta arquivada")
        void atualizarMetaArquivadaErro() {
            mockVinculoSucesso();
            Meta metaArquivada = Meta.builder().id(50L).aluno(alunoMock).status(StatusMeta.ARQUIVADA).build();
            when(metaRepository.findById(50L)).thenReturn(Optional.of(metaArquivada));

            UpdateMetaCommand cmd = new UpdateMetaCommand(
                    "Novo Titulo",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> metaService.atualizar(USER_ID, MATRICULA, 50L, cmd));
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("Deve concluir meta automaticamente ao atingir o valor atual")
        void atualizarProgressoConcluirMeta() {
            mockVinculoSucesso();
            Meta meta = Meta.builder()
                    .id(50L).aluno(alunoMock).valorAlvo(2).valorAtual(0)
                    .tipoValidacao(TipoValidacaoMeta.PERCENTUAL).status(StatusMeta.ATIVA).build();

            when(metaRepository.findById(50L)).thenReturn(Optional.of(meta));
            when(metaRepository.save(ArgumentMatchers.<Meta>any())).thenAnswer(i -> i.getArgument(0));

            AtualizarProgressoCommand cmd = new AtualizarProgressoCommand(2);
            MetaResponse resp = metaService.atualizarProgressoManual(USER_ID, MATRICULA, 50L, cmd);

            assertThat(resp.status()).isEqualTo(StatusMeta.CONCLUIDA);
            assertThat(meta.getConcluidaEm()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Testes de Automação de Leitura")
    class AutomacaoLeituraTests {

        @Test
        @DisplayName("Deve incrementar progresso de meta LIVROS_LIDOS ao registrar leitura")
        void registrarLeituraSucesso() {
            Meta metaLeitura = Meta.builder()
                    .id(1L).aluno(alunoMock).tipo(TipoMeta.LIVROS_LIDOS)
                    .valorAlvo(5).valorAtual(1).status(StatusMeta.ATIVA)
                    .tipoValidacao(TipoValidacaoMeta.PERCENTUAL)
                    .dataInicio(LocalDate.now().minusDays(1))
                    .dataFim(LocalDate.now().plusDays(1))
                    .build();

            when(metaRepository.findByAlunoIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
                    any(), any(), any(), any()))
                    .thenReturn(List.of(metaLeitura));

            metaService.registrarLeituraConcluida(alunoMock, new Livro(), LocalDate.now());

            assertThat(metaLeitura.getValorAtual()).isEqualTo(2);
            verify(metaRepository).save(metaLeitura);
        }

        @Test
        @DisplayName("Deve incrementar meta de palavra-chave apenas se o título do livro bater")
        void registrarLeituraPalavraChave() {
            Meta metaHarry = Meta.builder()
                    .id(1L).aluno(alunoMock).tipo(TipoMeta.LIVROS_COM_PALAVRA_CHAVE)
                    .filtroValor("Harry")
                    .valorAlvo(2).valorAtual(0).status(StatusMeta.ATIVA)
                    .tipoValidacao(TipoValidacaoMeta.PERCENTUAL)
                    .dataInicio(LocalDate.now().minusDays(1))
                    .dataFim(LocalDate.now().plusDays(1))
                    .build();

            when(metaRepository.findByAlunoIdAndStatusAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
                    any(), any(), any(), any()))
                    .thenReturn(List.of(metaHarry));

            // Livro que não condiz
            metaService.registrarLeituraConcluida(alunoMock, Livro.builder().titulo("Percy Jackson").build(), LocalDate.now());
            assertThat(metaHarry.getValorAtual()).isEqualTo(0);

            // Livro que condiz
            metaService.registrarLeituraConcluida(alunoMock, Livro.builder().titulo("Harry Potter e a Pedra").build(), LocalDate.now());
            assertThat(metaHarry.getValorAtual()).isEqualTo(1);
        }
    }
}
