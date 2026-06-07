package com.br.lottus.mobile.dashboard.controller;

import com.br.lottus.mobile.atividade.command.AtividadeResponse;
import com.br.lottus.mobile.common.entity.ApiResponse;
import com.br.lottus.mobile.dashboard.command.EvolucaoMensalResponse;
import com.br.lottus.mobile.dashboard.command.GeneroLeituraResponse;
import com.br.lottus.mobile.dashboard.command.ResumoDashboardResponse;
import com.br.lottus.mobile.dashboard.service.DashboardService;
import com.br.lottus.mobile.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alunos/{matricula}/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "KPIs agregados do aluno vinculado para visao do responsavel")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    @Operation(
            summary = "Resumo de KPIs do mes",
            description = "Retorna em uma unica chamada os indicadores numericos: livros lidos no mes, dias com o livro atual e percentual medio das metas mensais"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resumo calculado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Aluno nao vinculado ao responsavel"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Aluno nao encontrado")
    })
    public ResponseEntity<ApiResponse<ResumoDashboardResponse>> resumo(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Matricula do aluno") @PathVariable String matricula) {
        ResumoDashboardResponse resumo = dashboardService.resumo(usuario.getId(), matricula);
        return ResponseEntity.ok(ApiResponse.ok(resumo));
    }

    @GetMapping("/generos")
    @Operation(
            summary = "Distribuicao por genero/categoria",
            description = "Retorna o percentual de leituras concluidas em cada categoria, ordenado do mais lido para o menos lido"
    )
    public ResponseEntity<ApiResponse<List<GeneroLeituraResponse>>> generos(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula) {
        List<GeneroLeituraResponse> generos = dashboardService.generos(usuario.getId(), matricula);
        return ResponseEntity.ok(ApiResponse.ok(generos));
    }

    @GetMapping("/evolucao-mensal")
    @Operation(
            summary = "Evolucao mensal de leituras",
            description = "Retorna em uma unica chamada o total de livros lidos por mes nos ultimos N meses (default 12, maximo 24). Inclui meses sem leituras com total=0 para alimentar diretamente o grafico"
    )
    public ResponseEntity<ApiResponse<List<EvolucaoMensalResponse>>> evolucaoMensal(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula,
            @Parameter(description = "Quantidade de meses retroativos a considerar (1-24, default 12)")
            @RequestParam(required = false) Integer meses) {
        List<EvolucaoMensalResponse> evolucao = dashboardService.evolucaoMensal(usuario.getId(), matricula, meses);
        return ResponseEntity.ok(ApiResponse.ok(evolucao));
    }

    @GetMapping("/atividades")
    @Operation(
            summary = "Ultimas atividades",
            description = "Feed cronologico das ultimas acoes do responsavel relacionadas ao aluno (livro iniciado/concluido, meta criada/concluida). Cada item traz referenciaTipo e referenciaId para o frontend redirecionar"
    )
    public ResponseEntity<ApiResponse<List<AtividadeResponse>>> atividades(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula,
            @Parameter(description = "Quantidade maxima de eventos (1-50, default 20)")
            @RequestParam(required = false) Integer limit) {
        List<AtividadeResponse> atividades = dashboardService.atividades(usuario.getId(), matricula, limit);
        return ResponseEntity.ok(ApiResponse.ok(atividades));
    }
}
