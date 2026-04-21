package com.br.lottus.mobile.meta.entity;

import com.br.lottus.mobile.aluno.entity.Aluno;
import com.br.lottus.mobile.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_usuario_id", nullable = false)
    private Usuario criadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoMeta tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_validacao", nullable = false, length = 20)
    private TipoValidacaoMeta tipoValidacao;

    @Column(name = "valor_alvo", nullable = false)
    @Builder.Default
    private Integer valorAlvo = 1;

    @Column(name = "valor_atual", nullable = false)
    @Builder.Default
    private Integer valorAtual = 0;

    @Column(name = "filtro_valor", length = 150)
    private String filtroValor;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusMeta status = StatusMeta.ATIVA;

    @Column(name = "concluida_em")
    private LocalDateTime concluidaEm;

    @Column(name = "criada_em", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadaEm = LocalDateTime.now();

    @Column(name = "atualizada_em", nullable = false)
    @Builder.Default
    private LocalDateTime atualizadaEm = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.atualizadaEm = LocalDateTime.now();
    }

    public int percentual() {
        if (valorAlvo == null || valorAlvo <= 0) return 0;
        int pct = (int) Math.floor((valorAtual * 100.0) / valorAlvo);
        return Math.max(0, Math.min(100, pct));
    }

    public boolean atingida() {
        return valorAtual != null && valorAlvo != null && valorAtual >= valorAlvo;
    }
}
