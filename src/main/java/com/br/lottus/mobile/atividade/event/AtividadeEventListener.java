package com.br.lottus.mobile.atividade.event;

import com.br.lottus.mobile.atividade.entity.Atividade;
import com.br.lottus.mobile.atividade.repository.AtividadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AtividadeEventListener {

    private final AtividadeRepository atividadeRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(AtividadeRegistradaEvent evento) {
        try {
            Atividade atividade = Atividade.builder()
                    .alunoId(evento.alunoId())
                    .usuarioId(evento.usuarioId())
                    .tipo(evento.tipo())
                    .referenciaTipo(evento.referenciaTipo())
                    .referenciaId(evento.referenciaId())
                    .tituloResumo(evento.tituloResumo())
                    .ocorridoEm(evento.ocorridoEm())
                    .build();
            atividadeRepository.save(atividade);
            log.debug("Atividade registrada tipo={} aluno={} ref={}/{}",
                    evento.tipo(), evento.alunoId(), evento.referenciaTipo(), evento.referenciaId());
        } catch (Exception e) {
            log.error("Falha ao registrar atividade tipo={} aluno={}", evento.tipo(), evento.alunoId(), e);
        }
    }
}
