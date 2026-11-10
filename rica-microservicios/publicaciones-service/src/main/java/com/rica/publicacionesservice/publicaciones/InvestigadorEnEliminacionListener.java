package com.rica.publicacionesservice.publicaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Paso de publicaciones en la Saga de eliminación: marca como huérfanas las
 * publicaciones del investigador y confirma por publicaciones.marcadas-huerfanas.
 * La confirmación se envía sin outbox para no alargar el taller.
 */
@Component
public class InvestigadorEnEliminacionListener {

    private static final Logger log = LoggerFactory.getLogger(InvestigadorEnEliminacionListener.class);

    private final PublicacionRepository publicacionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InvestigadorEnEliminacionListener(PublicacionRepository publicacionRepository,
                                             KafkaTemplate<String, String> kafkaTemplate,
                                             ObjectMapper objectMapper) {
        this.publicacionRepository = publicacionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "investigadores.en-eliminacion")
    public void alIniciarseEliminacion(String mensaje) {
        JsonNode evento = objectMapper.readTree(mensaje);
        String correo = evento.get("correoInstitucional").asString();
        String correlationId = evento.has("correlationId") ? evento.get("correlationId").asString() : "sin-correlacion";

        // putCloseable restaura el MDC al terminar, pase lo que pase.
        try (MDC.MDCCloseable ignorado = MDC.putCloseable("correlationId", correlationId)) {
            List<Publicacion> publicaciones = publicacionRepository.findByInvestigadorCorreo(correo);
            publicaciones.forEach(Publicacion::marcarComoHuerfana);
            publicacionRepository.saveAll(publicaciones);
            log.info("Saga: {} publicaciones de {} marcadas como huérfanas", publicaciones.size(), correo);

            // La confirmación lleva el mismo id: la traza continúa en el siguiente paso
            String confirmacion = objectMapper.writeValueAsString(Map.of(
                    "correoInstitucional", correo,
                    "cantidad", publicaciones.size(),
                    "correlationId", correlationId));
            kafkaTemplate.send("publicaciones.marcadas-huerfanas", correo, confirmacion);
        }
    }
}
