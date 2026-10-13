package com.rica.ricaapi.investigadores.infraestructura.entrada.web;

import com.rica.ricaapi.investigadores.dominio.Investigador;
import org.springframework.stereotype.Component;

@Component
public class InvestigadorMapper {

    public InvestigadorResponse aResponse(Investigador investigador) {
        return new InvestigadorResponse(
            investigador.getId(),
            investigador.getNombreCompleto(),
            investigador.getCorreoInstitucional().valor(),
            investigador.getGrupoInvestigacion()
        );
    }
}
