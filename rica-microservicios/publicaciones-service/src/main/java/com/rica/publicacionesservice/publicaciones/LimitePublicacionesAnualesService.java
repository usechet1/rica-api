package com.rica.publicacionesservice.publicaciones;

import org.springframework.stereotype.Service;

/**
 * Servicio de Dominio: la regla "máximo 5 publicaciones por investigador por año"
 * no pertenece a Investigador (no la puede verificar solo) ni a Publicacion
 * (tampoco: necesita contar las demás) — involucra a varios objetos y no
 * guarda estado propio.
 *
 * Vive deliberadamente en el paquete publicaciones, no en investigadores:
 * publicaciones solo conoce el correo del investigador (un String), nunca
 * la clase Investigador, que vive en investigadores-service.
 */
@Service
public class LimitePublicacionesAnualesService {

    private static final int MAXIMO_POR_ANIO = 5;

    private final PublicacionRepository publicacionRepository;

    public LimitePublicacionesAnualesService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    public boolean puedeRegistrar(Publicacion nueva) {
        long registradasEsteAnio = publicacionRepository.countByInvestigadorCorreoAndAnio(
            nueva.getInvestigadorCorreo(), nueva.getAnio());
        return registradasEsteAnio < MAXIMO_POR_ANIO;
    }
}
