package com.rica.ricaapi.investigadores;

public class InvestigadorResponse {

    private Long id;
    private String nombreCompleto;
    private String correoInstitucional;
    private String grupoInvestigacion;

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
