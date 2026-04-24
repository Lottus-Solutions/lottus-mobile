package com.br.lottus.mobile.livro.controller;

import com.br.lottus.mobile.common.entity.ApiResponse;
import com.br.lottus.mobile.livro.command.LivroResponse;
import com.br.lottus.mobile.livro.entity.Livro;
import com.br.lottus.mobile.livro.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/livros")
@Tag(name = "Livros", description = "Busca um livro pelo seu id")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por id")
    public ResponseEntity<ApiResponse<LivroResponse>> buscarPorId(@PathVariable Long id) {
        Livro livro = service.buscarPorId(id);
        LivroResponse livroResponse = LivroResponse.from(livro);

        return ResponseEntity.ok(ApiResponse.ok(livroResponse));
    }

}
