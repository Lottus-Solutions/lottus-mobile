package com.br.lottus.mobile.emprestimo.controller;

import com.br.lottus.mobile.common.entity.ApiResponse;
import com.br.lottus.mobile.emprestimo.command.CreateEmprestimoCommand;
import com.br.lottus.mobile.emprestimo.command.EmprestimoResponse;
import com.br.lottus.mobile.emprestimo.service.EmprestimoService;
import com.br.lottus.mobile.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos/{matricula}/emprestimos")
@RequiredArgsConstructor
@Tag(name = "Emprestimos", description = "Historico de leituras do aluno vinculado ao responsavel")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @PostMapping
    @Operation(summary = "Registrar nova leitura", description = "O responsavel registra uma nova leitura para um aluno vinculado")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Leitura registrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Aluno nao vinculado ao responsavel"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Aluno ou livro nao encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Aluno ja possui leitura em andamento")
    })
    public ResponseEntity<ApiResponse<EmprestimoResponse>> registrarLeitura(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Matricula do aluno") @PathVariable String matricula,
            @Valid @RequestBody CreateEmprestimoCommand command) {
        EmprestimoResponse response = emprestimoService.registrarLeitura(usuario.getId(), matricula, command);
        return ResponseEntity.status(201).body(ApiResponse.ok("Leitura registrada", response));
    }

    @GetMapping("/atual")
    @Operation(summary = "Livro atual em leitura", description = "Retorna a leitura em andamento do aluno, ou vazio se nao houver")
    public ResponseEntity<ApiResponse<EmprestimoResponse>> livroAtual(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula) {
        return emprestimoService.buscarLivroAtual(usuario.getId(), matricula)
                .map(r -> ResponseEntity.ok(ApiResponse.ok(r)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("Sem leitura em andamento", null)));
    }

    @GetMapping
    @Operation(summary = "Historico de leituras", description = "Retorna o historico completo de leituras do aluno")
    public ResponseEntity<ApiResponse<List<EmprestimoResponse>>> historico(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula) {
        List<EmprestimoResponse> historico = emprestimoService.buscarHistorico(usuario.getId(), matricula);
        return ResponseEntity.ok(ApiResponse.ok(historico));
    }

    @PostMapping("/{emprestimoId}/concluir")
    @Operation(summary = "Concluir leitura", description = "Marca a leitura como finalizada e registra a data de devolucao")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leitura concluida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Leitura nao pertence ao aluno"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Leitura nao encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Leitura ja concluida")
    })
    public ResponseEntity<ApiResponse<EmprestimoResponse>> concluir(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula,
            @PathVariable Long emprestimoId) {
        EmprestimoResponse response = emprestimoService.concluirLeitura(usuario.getId(), matricula, emprestimoId);
        return ResponseEntity.ok(ApiResponse.ok("Leitura concluida", response));
    }
}
