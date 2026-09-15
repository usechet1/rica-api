package com.rica.ricaapi.investigadores;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Único punto de construcción de un Investigador. Nadie fuera de esta clase
 * debería poder ensamblar un Investigador a medias: aquí se valida el correo
 * (vía el Value Object) y se rechaza el correo duplicado, de forma atómica.
 */
@Component
public class InvestigadorFactory {

    private final InvestigadorRepository investigadorRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InvestigadorFactory(InvestigadorRepository investigadorRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.investigadorRepository = investigadorRepository;
        this.eventPublisher = eventPublisher;
    }

    public Investigador crear(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        CorreoInstitucional correo = new CorreoInstitucional(correoInstitucional);

        if (investigadorRepository.existsByCorreoInstitucional_Valor(correo.valor())) {
            throw new CorreoDuplicadoException(
                "Ya existe un investigador registrado con el correo " + correo.valor());
        }

        Investigador investigador = new Investigador(null, nombreCompleto, correo, grupoInvestigacion);

        // Reto extra (paso 7, opcional): Domain Event. No cambia el comportamiento
        // observable; hoy el "otro lado" del evento es solo un log.
        eventPublisher.publishEvent(new InvestigadorRegistrado(correo.valor(), Instant.now()));

        return investigador;
    }
}
