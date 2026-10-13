package com.rica.ricaapi.investigadores.dominio;

import java.time.Instant;

/** Evento de dominio (reto extra del Taller de la Lección 3, se conserva aquí). */
public record InvestigadorRegistrado(String correoInstitucional, Instant ocurridoEn) {
}
