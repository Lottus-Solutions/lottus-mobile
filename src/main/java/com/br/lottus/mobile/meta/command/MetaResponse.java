package com.br.lottus.mobile.meta.command;

import com.br.lottus.mobile.meta.entity.Meta;
import com.br.lottus.mobile.meta.entity.StatusMeta;
import com.br.lottus.mobile.meta.entity.TipoMeta;
import com.br.lottus.mobile.meta.entity.TipoValidacaoMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Schema(description = "Representacao de uma meta")
public record MetaResponse(

        Long id,
        String alunoMatricula,
        TipoMeta tipo,
        String titulo,
        String descricao,
        TipoValidacaoMeta tipoValidacao,
        Integer valorAlvo,
        Integer valorAtual,
        Integer percentual,
        String filtroValor,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusMeta status,
        LocalDateTime concluidaEm,
        LocalDateTime criadaEm
) {

    public static MetaResponse from(Meta m) {
        return MetaResponse.builder()
                .id(m.getId())
                .alunoMatricula(m.getAluno() != null ? m.getAluno().getMatricula() : null)
                .tipo(m.getTipo())
                .titulo(m.getTitulo())
                .descricao(m.getDescricao())
                .tipoValidacao(m.getTipoValidacao())
                .valorAlvo(m.getValorAlvo())
                .valorAtual(m.getValorAtual())
                .percentual(m.percentual())
                .filtroValor(m.getFiltroValor())
                .dataInicio(m.getDataInicio())
                .dataFim(m.getDataFim())
                .status(m.getStatus())
                .concluidaEm(m.getConcluidaEm())
                .criadaEm(m.getCriadaEm())
                .build();
    }
}
