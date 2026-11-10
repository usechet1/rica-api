package com.rica.investigadoresservice.investigadores.infraestructura.salida.mensajeria;

import com.rica.investigadoresservice.investigadores.aplicacion.puertos.PublicadorEventos;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador de PublicadorEventos: en vez de hablar con Kafka, guarda el evento
 * en la tabla outbox, dentro de la misma transacción que el investigador.
 * OutboxPoller se encarga después de enviarlo a Kafka.
 */
@Component
public class OutboxPublicadorEventos implements PublicadorEventos {

    private final OutboxEventRepository repositorio;
    private final ObjectMapper objectMapper;

    public OutboxPublicadorEventos(OutboxEventRepository repositorio, ObjectMapper objectMapper) {
        this.repositorio = repositorio;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(String topico, String clave, Map<String, Object> contenido) {
        // El id de correlación viaja dentro del evento: así la traza cruza servicios y Kafka.
        // Los consumidores que no conocen el campo nuevo simplemente lo ignoran.
        Map<String, Object> conTraza = new HashMap<>(contenido);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            conTraza.put("correlationId", correlationId);
        }
        String payload = objectMapper.writeValueAsString(conTraza);
        repositorio.save(new OutboxEvent(topico, clave, payload));
    }
}
