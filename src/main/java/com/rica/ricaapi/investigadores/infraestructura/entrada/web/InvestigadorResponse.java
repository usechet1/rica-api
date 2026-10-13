package com.rica.ricaapi.investigadores.infraestructura.entrada.web;

public class InvestigadorResponse {

    private final Long id;
    private final String nombreCompleto;
    private final String correoInstitucional;
    private final String grupoInvestigacion;

    public InvestigadorResponse(Long id, String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
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

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public String getGrupoInvestigacion() {
        return grupoInvestigacion;
    }
}
