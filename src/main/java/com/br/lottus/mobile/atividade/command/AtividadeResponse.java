package com.br.lottus.mobile.atividade.command;

import com.br.lottus.mobile.atividade.entity.Atividade;
import com.br.lottus.mobile.atividade.entity.TipoAtividade;
import com.br.lottus.mobile.atividade.entity.TipoReferenciaAtividade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Representa um evento ocorrido (livro iniciado/concluido, meta criada/concluida) para o feed de atividades")
public record AtividadeResponse(

        Long id,

        @Schema(description = "Tipo do evento")
        TipoAtividade tipo,

        @Schema(description = "Tipo do recurso referenciado para permitir navegacao no frontend")
        TipoReferenciaAtividade referenciaTipo,

        @Schema(description = "Id do recurso referenciado (ex.: livro.id ou meta.id)")
        Long referenciaId,

        @Schema(description = "Texto resumido pronto para exibicao")
        String tituloResumo,

        @Schema(description = "Data/hora exata em que o evento ocorreu")
        LocalDateTime ocorridoEm
) {

    public static AtividadeResponse from(Atividade a) {
        return AtividadeResponse.builder()
                .id(a.getId())
                .tipo(a.getTipo())
                .referenciaTipo(a.getReferenciaTipo())
                .referenciaId(a.getReferenciaId())
                .tituloResumo(a.getTituloResumo())
                .ocorridoEm(a.getOcorridoEm())
                .build();
    }
}
