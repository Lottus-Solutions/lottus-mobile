package com.br.lottus.mobile.livro.repository;

import com.br.lottus.mobile.livro.entity.Livro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
class LivroRepositoryTest {

    @Autowired
    private LivroRepository repository;

    @Test
    void deveBuscarPorTituloOuAutor() {
        Livro livro = new Livro();
        livro.setTitulo("Clean Code");
        livro.setAutor("Robert Martin");

        repository.save(livro);

        var resultado = repository
                .buscarPorTituloOuAutor("clean");

        assertFalse(resultado.isEmpty());
    }
}
