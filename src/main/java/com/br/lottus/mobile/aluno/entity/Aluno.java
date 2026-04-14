package com.br.lottus.mobile.aluno.entity;

import com.br.lottus.mobile.emprestimo.entity.Emprestimo;
import com.br.lottus.mobile.turma.entity.Turma;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alunos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String matricula;

    @Column(nullable = false)
    private String nome;

    @Column(name = "qtd_bonus", nullable = false)
    @Builder.Default
    private Double qtdBonus = 0.0;

    @Column(name = "qtd_livros_lidos", nullable = false)
    @Builder.Default
    private Integer qtdLivrosLidos = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @OneToMany(mappedBy = "aluno", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Emprestimo> emprestimos = new ArrayList<>();
}
