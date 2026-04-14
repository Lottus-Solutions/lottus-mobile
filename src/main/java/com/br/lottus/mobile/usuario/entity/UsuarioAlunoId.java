package com.br.lottus.mobile.usuario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioAlunoId implements Serializable {

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "aluno_id")
    private Long alunoId;
}
