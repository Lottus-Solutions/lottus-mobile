package com.br.lottus.mobile.aluno.controller;

import com.br.lottus.mobile.aluno.command.AlunoResponse;
import com.br.lottus.mobile.aluno.service.AlunoVerificationService;
import com.br.lottus.mobile.common.entity.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alunos")
@RequiredArgsConstructor
@Tag(name = "Alunos", description = "Verificacao publica de matricula (RA) para cadastro de responsavel")
public class AlunoController {

    private final AlunoVerificationService alunoVerificationService;

    @GetMapping("/verificar-ra/{matricula}")
    @Operation(
            summary = "Verificar matricula (RA)",
            description = "Endpoint publico que verifica se a matricula existe e se o aluno ja esta vinculado a um responsavel")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Aluno encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Aluno nao encontrado")
    })
    public ResponseEntity<ApiResponse<AlunoResponse>> verificarMatricula(
            @Parameter(description = "Matricula (RA) do aluno", example = "2024001")
            @PathVariable String matricula) {
        AlunoResponse response = alunoVerificationService.verificarMatricula(matricula);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
