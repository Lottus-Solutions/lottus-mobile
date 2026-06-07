package com.br.lottus.mobile.dashboard.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Resumo numerico dos KPIs do dashboard do responsavel")
public record ResumoDashboardResponse(

        @Schema(description = "Quantidade de livros concluidos dentro do mes atual")
        long livrosLidosNoMes,

        @Schema(description = "Dias decorridos desde o inicio da leitura atual; null quando nao ha leitura em andamento")
        Integer diasComLivroAtual,

        @Schema(description = "Titulo do livro em leitura no momento; null quando nao ha leitura em andamento")
        String tituloLivroAtual,

        @Schema(description = "Percentual medio (0 a 100) das metas que abrangem o mes atual e nao estao arquivadas")
        int percentualMetasMensais,

        @Schema(description = "Quantidade de metas que abrangem o mes atual e nao estao arquivadas")
        int totalMetasMensais,

        @Schema(description = "Quantidade de metas mensais ja concluidas")
        int metasMensaisConcluidas
) {
}
