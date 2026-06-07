package com.br.lottus.mobile.atividade.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "atividades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoAtividade tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo", nullable = false, length = 20)
    private TipoReferenciaAtividade referenciaTipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "titulo_resumo", nullable = false, length = 255)
    private String tituloResumo;

    @Column(name = "ocorrido_em", nullable = false)
    @Builder.Default
    private LocalDateTime ocorridoEm = LocalDateTime.now();
}
