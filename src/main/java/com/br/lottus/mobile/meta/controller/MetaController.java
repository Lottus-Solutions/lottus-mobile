package com.br.lottus.mobile.meta.controller;

import com.br.lottus.mobile.common.entity.ApiResponse;
import com.br.lottus.mobile.meta.command.AtualizarProgressoCommand;
import com.br.lottus.mobile.meta.command.CreateMetaCommand;
import com.br.lottus.mobile.meta.command.MetaResponse;
import com.br.lottus.mobile.meta.command.UpdateMetaCommand;
import com.br.lottus.mobile.meta.entity.StatusMeta;
import com.br.lottus.mobile.meta.service.MetaService;
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
@RequestMapping("/api/alunos/{matricula}/metas")
@RequiredArgsConstructor
@Tag(name = "Metas", description = "Metas de leitura definidas pelo responsavel para o aluno vinculado")
public class MetaController {

    private final MetaService metaService;

    @PostMapping
    @Operation(summary = "Criar meta", description = "O responsavel cria uma nova meta para um aluno vinculado")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Meta criada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Aluno nao vinculado")
    })
    public ResponseEntity<ApiResponse<MetaResponse>> criar(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Matricula do aluno") @PathVariable String matricula,
            @Valid @RequestBody CreateMetaCommand command) {
        MetaResponse response = metaService.criar(usuario.getId(), matricula, command);
        return ResponseEntity.status(201).body(ApiResponse.ok("Meta criada", response));
    }

    @GetMapping
    @Operation(summary = "Listar metas", description = "Lista as metas do aluno. Aceita filtro opcional por status")
    public ResponseEntity<ApiResponse<List<MetaResponse>>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula,
            @RequestParam(required = false) StatusMeta status) {
        List<MetaResponse> metas = metaService.listar(usuario.getId(), matricula, status);
        return ResponseEntity.ok(ApiResponse.ok(metas));
    }

    @GetMapping("/{metaId}")
    @Operation(summary = "Obter meta")
    public ResponseEntity<ApiResponse<MetaResponse>> buscar(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula,
            @PathVariable Long metaId) {
        return ResponseEntity.ok(ApiResponse.ok(metaService.buscar(usuario.getId(), matricula, metaId)));
    }

    @PutMapping("/{metaId}")
    @Operation(summary = "Atualizar meta", description = "Atualiza campos editaveis. Tipo e aluno nao podem mudar")
    public ResponseEntity<ApiResponse<MetaResponse>> atualizar(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula,
            @PathVariable Long metaId,
            @Valid @RequestBody UpdateMetaCommand command) {
        MetaResponse response = metaService.atualizar(usuario.getId(), matricula, metaId, command);
        return ResponseEntity.ok(ApiResponse.ok("Meta atualizada", response));
    }

    @PatchMapping("/{metaId}/progresso")
    @Operation(summary = "Atualizar progresso manualmente")
    public ResponseEntity<ApiResponse<MetaResponse>> atualizarProgresso(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula,
            @PathVariable Long metaId,
            @Valid @RequestBody AtualizarProgressoCommand command) {
        MetaResponse response = metaService.atualizarProgressoManual(usuario.getId(), matricula, metaId, command);
        return ResponseEntity.ok(ApiResponse.ok("Progresso atualizado", response));
    }

    @DeleteMapping("/{metaId}")
    @Operation(summary = "Remover meta")
    public ResponseEntity<Void> remover(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String matricula,
            @PathVariable Long metaId) {
        metaService.remover(usuario.getId(), matricula, metaId);
        return ResponseEntity.noContent().build();
    }
}
