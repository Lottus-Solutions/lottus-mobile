package com.br.lottus.mobile.emprestimo.entity;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.livro.entity.Livro;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "emprestimos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDate dataDevolucaoPrevista;

    @Column(name = "data_devolucao_efetiva")
    private LocalDate dataDevolucaoEfetiva;

    @Column(name = "dias_atrasados", nullable = false)
    @Builder.Default
    private int diasAtrasados = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_emprestimo", nullable = false, length = 20)
    @Builder.Default
    private StatusEmprestimo statusEmprestimo = StatusEmprestimo.ATIVO;
}
