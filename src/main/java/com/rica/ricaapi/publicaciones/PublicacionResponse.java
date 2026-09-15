package com.rica.ricaapi.publicaciones;

public class PublicacionResponse {

    private String id;
    private String titulo;
    private String investigadorCorreo;
    private Integer anio;

    public PublicacionResponse(String id, String titulo, String investigadorCorreo, Integer anio) {
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

    public String getInvestigadorCorreo() {
        return investigadorCorreo;
    }

    public Integer getAnio() {
        return anio;
    }
}
