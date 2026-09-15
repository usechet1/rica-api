package com.rica.ricaapi.investigadores;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Raíz del Agregado Investigador.
 *
 * Límite del Agregado (ver NOTAS.md para el razonamiento completo):
 *   - Dentro del límite: id, nombreCompleto, correoInstitucional (Value Object),
 *     grupoInvestigacion.
 *   - Publicacion queda FUERA de este límite a propósito: se referencia solo
 *     por el correo del investigador (String), nunca se carga la lista completa
 *     de publicaciones dentro de Investigador.
 */
@Entity
public class Investigador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Embedded
    @AttributeOverride(
        name = "valor",
        column = @Column(name = "correo_institucional", nullable = false, unique = true, length = 150)
    )
    private CorreoInstitucional correoInstitucional;

    @Column(name = "grupo_investigacion", length = 100)
    private String grupoInvestigacion;

    protected Investigador() {
        // requerido por JPA
    }

    public Investigador(Long id, String nombreCompleto, CorreoInstitucional correoInstitucional,
                         String grupoInvestigacion) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correoInstitucional = correoInstitucional;
        this.grupoInvestigacion = grupoInvestigacion;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public CorreoInstitucional getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(CorreoInstitucional correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getGrupoInvestigacion() {
        return grupoInvestigacion;
    }

    public void setGrupoInvestigacion(String grupoInvestigacion) {
        this.grupoInvestigacion = grupoInvestigacion;
    }

    // La igualdad de una Entidad se basa solo en su identidad (id),
    // nunca en el valor de sus atributos.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Investigador otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
