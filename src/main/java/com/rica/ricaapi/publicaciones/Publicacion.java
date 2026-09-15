package com.rica.ricaapi.publicaciones;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Publicacion referencia al Agregado Investigador solo por identidad
 * (investigadorCorreo es un String, nunca un objeto Investigador completo) —
 * la regla de Vernon de referenciar otros Agregados por id.
 */
@Document(collection = "publicaciones")
public class Publicacion {

    @Id
    private String id;

    private String titulo;
    private String investigadorCorreo;
    private Integer anio;

    protected Publicacion() {
    }

    public Publicacion(String id, String titulo, String investigadorCorreo, Integer anio) {
        this.id = id;
        this.titulo = titulo;
        this.investigadorCorreo = investigadorCorreo;
        this.anio = anio;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getInvestigadorCorreo() {
        return investigadorCorreo;
    }

    public void setInvestigadorCorreo(String investigadorCorreo) {
        this.investigadorCorreo = investigadorCorreo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }
}
