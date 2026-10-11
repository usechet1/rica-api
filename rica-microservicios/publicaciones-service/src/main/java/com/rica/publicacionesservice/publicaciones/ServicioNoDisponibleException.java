package com.rica.publicacionesservice.publicaciones;

/**
 * "No pude averiguar si el investigador existe": distinto de
 * RecursoNoEncontradoException ("el investigador no existe"). Se responde 503.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
