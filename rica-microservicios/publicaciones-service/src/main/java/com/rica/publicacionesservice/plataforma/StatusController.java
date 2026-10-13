package com.rica.publicacionesservice.plataforma;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Cada servicio expone su propio /api/status: ya no hay un único desplegable
 * que responda por todos.
 */
@RestController
public class StatusController {

    @GetMapping("/api/status")
    public Map<String, String> status() {
        return Map.of("servicio", "publicaciones-service", "estado", "UP");
    }
}
