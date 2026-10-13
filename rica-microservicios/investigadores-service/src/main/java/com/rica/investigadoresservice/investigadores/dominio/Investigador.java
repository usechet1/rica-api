package com.rica.investigadoresservice.investigadores.dominio;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Raíz del Agregado Investigador — núcleo del hexágono.
 *
 * Límite del Agregado (ver NOTAS.md): id, nombreCompleto, correoInstitucional
 * (Value Object), grupoInvestigacion. Publicacion queda fuera a propósito.
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInvestigador estado = EstadoInvestigador.ACTIVO;

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

    public void setId(Long id) {
        this.id = id;
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

    public EstadoInvestigador getEstado() {
        return estado;
    }

    public boolean estaEnEliminacion() {
        return estado == EstadoInvestigador.EN_ELIMINACION;
    }

    public void marcarEnEliminacion() {
        this.estado = EstadoInvestigador.EN_ELIMINACION;
    }

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
