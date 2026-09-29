package com.rica.ricaapi.investigadores.aplicacion;

import com.rica.ricaapi.investigadores.dominio.CorreoDuplicadoException;
import com.rica.ricaapi.investigadores.dominio.CorreoInstitucional;
import com.rica.ricaapi.investigadores.dominio.Investigador;
import com.rica.ricaapi.investigadores.dominio.InvestigadorRegistrado;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Único punto de construcción de un Investigador.
 *
 * NOTA sobre este taller: el enunciado instancia la Factory con un solo
 * argumento (el repositorio). Aquí se conserva el ApplicationEventPublisher
 * que se agregó como reto extra del Taller de la Lección 3 (evento de dominio
 * InvestigadorRegistrado) — por eso el constructor sigue teniendo dos
 * parámetros. El Fake del paso 5 se adapta pasando un publisher "no-op".
 */
@Component
public class InvestigadorFactory {

    private final RepositorioInvestigadores repositorioInvestigadores;
    private final ApplicationEventPublisher eventPublisher;

    public InvestigadorFactory(RepositorioInvestigadores repositorioInvestigadores,
                                ApplicationEventPublisher eventPublisher) {
        this.repositorioInvestigadores = repositorioInvestigadores;
        this.eventPublisher = eventPublisher;
    }

    public Investigador crear(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        CorreoInstitucional correo = new CorreoInstitucional(correoInstitucional);

        if (repositorioInvestigadores.existeCorreo(correo.valor())) {
            throw new CorreoDuplicadoException(
                "Ya existe un investigador registrado con el correo " + correo.valor());
        }

        Investigador investigador = new Investigador(null, nombreCompleto, correo, grupoInvestigacion);

        eventPublisher.publishEvent(new InvestigadorRegistrado(correo.valor(), Instant.now()));

        return investigador;
    }
}
