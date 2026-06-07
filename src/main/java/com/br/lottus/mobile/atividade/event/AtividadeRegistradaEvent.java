package com.br.lottus.mobile.atividade.event;

import com.br.lottus.mobile.atividade.entity.TipoAtividade;
import com.br.lottus.mobile.atividade.entity.TipoReferenciaAtividade;

import java.time.LocalDateTime;

public record AtividadeRegistradaEvent(
        Long alunoId,
        Long usuarioId,
        TipoAtividade tipo,
        TipoReferenciaAtividade referenciaTipo,
        Long referenciaId,
        String tituloResumo,
        LocalDateTime ocorridoEm
) {

    public static AtividadeRegistradaEvent agora(
            Long alunoId,
            Long usuarioId,
            TipoAtividade tipo,
            TipoReferenciaAtividade referenciaTipo,
            Long referenciaId,
            String tituloResumo) {
        return new AtividadeRegistradaEvent(
                alunoId, usuarioId, tipo, referenciaTipo, referenciaId, tituloResumo, LocalDateTime.now());
    }
}
