package com.rica.ricaapi.publicaciones;

import org.springframework.stereotype.Component;

@Component
public class PublicacionMapper {

    public PublicacionResponse aResponse(Publicacion publicacion) {
        return new PublicacionResponse(
            publicacion.getId(),
            publicacion.getTitulo(),
            publicacion.getInvestigadorCorreo(),
            publicacion.getAnio()
        );
    }
}
