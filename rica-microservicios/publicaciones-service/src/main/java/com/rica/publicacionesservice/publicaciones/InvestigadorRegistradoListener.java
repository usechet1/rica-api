package com.rica.publicacionesservice.publicaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del evento investigadores.registrados. Por ahora solo lo deja en
 * el log: lo importante es que llega sin que investigadores-service llame
 * directamente a este servicio.
 */
@Component
public class InvestigadorRegistradoListener {

    private static final Logger log = LoggerFactory.getLogger(InvestigadorRegistradoListener.class);

    @KafkaListener(topics = "investigadores.registrados")
    public void alRegistrarseInvestigador(String mensaje) {
        log.info("Evento recibido [investigadores.registrados]: {}", mensaje);
    }
}
