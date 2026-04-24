package com.br.lottus.mobile.livro.repository;

import com.br.lottus.mobile.livro.entity.Livro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class LivroRepositoryTest {

    @Autowired
    private LivroRepository repository;

    @Test
    @DisplayName("Deve buscar livro por parte do título ignorando case")
    void buscaPorTitulo() {
        // Arrange
        Livro livro = Livro.builder()
                .titulo("Harry Potter")
                .autor("J.K. Rowling")
                .isbn("123")
                .totalPaginas(300)
                .build();
        repository.save(livro);

        // Act
        Page<Livro> result = repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(
                "harry", "harry", PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitulo()).isEqualTo("Harry Potter");
    }

    @Test
    @DisplayName("Deve buscar livro por parte do nome do autor")
    void buscaPorAutor() {
        // Arrange
        Livro livro = Livro.builder().titulo("O Hobbit").autor("Tolkien").isbn("456").totalPaginas(310).build();
        repository.save(livro);

        // Act
        Page<Livro> result = repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(
                "tolk", "tolk", PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAutor()).isEqualTo("Tolkien");
    }
}
