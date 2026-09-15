package com.rica.ricaapi.publicaciones;

import com.rica.ricaapi.investigadores.Investigador;
import org.springframework.stereotype.Service;

/**
 * Servicio de Dominio: la regla "máximo 5 publicaciones por investigador por año"
 * no pertenece a Investigador (no la puede verificar solo) ni a Publicacion
 * (tampoco: necesita contar las demás) — involucra a varios objetos y no
 * guarda estado propio.
 *
 * Vive deliberadamente en el paquete publicaciones, no en investigadores:
 * publicaciones ya depende de investigadores (lo consulta por su correo),
 * nunca al revés.
 */
@Service
public class LimitePublicacionesAnualesService {

    private static final int MAXIMO_POR_ANIO = 5;

    private final PublicacionRepository publicacionRepository;

    public LimitePublicacionesAnualesService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    public boolean puedeRegistrar(Investigador investigador, Publicacion nueva) {
        long registradasEsteAnio = publicacionRepository.countByInvestigadorCorreoAndAnio(
            investigador.getCorreoInstitucional().valor(), nueva.getAnio());
        return registradasEsteAnio < MAXIMO_POR_ANIO;
    }
}
