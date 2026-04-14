package com.br.lottus.mobile.usuario.entity;

import com.br.lottus.mobile.aluno.entity.Aluno;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_aluno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAluno {

    @EmbeddedId
    private UsuarioAlunoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("alunoId")
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;
}
