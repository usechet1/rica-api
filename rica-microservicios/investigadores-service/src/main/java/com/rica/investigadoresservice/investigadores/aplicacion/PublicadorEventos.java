package com.rica.investigadoresservice.investigadores.aplicacion;

import java.util.Map;

/**
 * Puerto secundario: "publicar un evento". La capa de aplicación no sabe si
 * detrás hay una tabla outbox, Kafka u otra cosa.
 */
public interface PublicadorEventos {

    void publicar(String topico, String clave, Map<String, Object> contenido);
}
