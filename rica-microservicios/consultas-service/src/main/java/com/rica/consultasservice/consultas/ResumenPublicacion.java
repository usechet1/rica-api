package com.rica.consultasservice.consultas;

/**
 * Copia desnormalizada de una publicación, embebida dentro de la ficha del investigador.
 * (Sin "tipo": las publicaciones de RICA no tienen ese campo.)
 */
public record ResumenPublicacion(String id, String titulo, Integer anio) {
}
