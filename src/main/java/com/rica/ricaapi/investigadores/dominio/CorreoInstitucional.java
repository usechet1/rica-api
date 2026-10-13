package com.rica.ricaapi.investigadores.dominio;

import jakarta.persistence.Embeddable;

/**
 * Value Object: un correo institucional válido siempre pertenece al dominio
 * @uptc.edu.co. Es imposible construir una instancia inválida — la regla
 * vive en un único lugar, en vez de estar dispersa en validaciones de formulario.
 *
 * Único import fuera de java.* es jakarta.persistence.Embeddable — tolerado
 * según la guía (sección 5): es una anotación de mapeo, no una dependencia de
 * comportamiento hacia un framework.
 */
@Embeddable
public record CorreoInstitucional(String valor) {

    public CorreoInstitucional {
        if (valor == null || !valor.endsWith("@uptc.edu.co")) {
            throw new IllegalArgumentException(
                "El correo institucional debe pertenecer al dominio @uptc.edu.co");
        }
    }
}
