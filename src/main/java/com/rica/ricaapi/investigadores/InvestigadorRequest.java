package com.rica.ricaapi.investigadores;

import jakarta.validation.constraints.NotBlank;

public class InvestigadorRequest {

    @NotBlank
    private String nombreCompleto;

    @NotBlank
    private String correoInstitucional;

    @NotBlank
    private String grupoInvestigacion;

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getGrupoInvestigacion() {
        return grupoInvestigacion;
    }

    public void setGrupoInvestigacion(String grupoInvestigacion) {
        this.grupoInvestigacion = grupoInvestigacion;
    }
}
