package com.rica.publicacionesservice.publicaciones;

/**
 * Puerto secundario: lo diseña publicaciones (quien lo consume), no la
 * tecnología del otro lado. Reemplaza la dependencia en memoria que antes
 * existía hacia RepositorioInvestigadores de investigadores.
 */
public interface VerificadorInvestigador {

    boolean existe(String correoInstitucional);
}
