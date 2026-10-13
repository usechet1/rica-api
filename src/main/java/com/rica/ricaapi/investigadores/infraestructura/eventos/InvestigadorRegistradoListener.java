package com.rica.ricaapi.investigadores.infraestructura.eventos;

import com.rica.ricaapi.investigadores.dominio.InvestigadorRegistrado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Adaptador que reacciona al evento de dominio; hoy solo hace log.info. */
@Component
public class InvestigadorRegistradoListener {

    private static final Logger log = LoggerFactory.getLogger(InvestigadorRegistradoListener.class);

    @EventListener
    public void onInvestigadorRegistrado(InvestigadorRegistrado evento) {
        log.info("Investigador registrado: {} en {}", evento.correoInstitucional(), evento.ocurridoEn());
    }
}
