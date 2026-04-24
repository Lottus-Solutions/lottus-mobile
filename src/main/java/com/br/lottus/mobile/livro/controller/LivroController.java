package com.br.lottus.mobile.livro.controller;

import com.br.lottus.mobile.common.entity.ApiResponse;
import com.br.lottus.mobile.livro.command.LivroResponse;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livros")
@RequiredArgsConstructor
@Tag(name = "Livros", description = "Listagem e busca de livros")
public class LivroController {

    private final LivroService service;

    @GetMapping
    @Operation(summary = "Lista ou busca livros pelo autor ou título")
    public ResponseEntity<ApiResponse<Page<LivroResponse>>> listar(
            @RequestParam(required = false) String busca,
            @PageableDefault(sort = "titulo", direction = Sort.Direction.ASC, size = 10) Pageable pageable
            ) {

        Page<LivroResponse> response = service.listarEBuscarPorTituloOuAutor(busca, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por id")
    public ResponseEntity<ApiResponse<LivroResponse>> buscarPorId(@PathVariable Long id) {
        Livro livro = service.buscarPorId(id);
        LivroResponse livroResponse = LivroResponse.from(livro);

        return ResponseEntity.ok(ApiResponse.ok(livroResponse));
    }

}
