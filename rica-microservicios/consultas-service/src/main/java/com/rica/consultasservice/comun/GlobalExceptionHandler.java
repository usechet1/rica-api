package com.rica.consultasservice.comun;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo de errores propio de consultas-service, con el mismo formato
 * ({"mensaje": ...}) que los demás servicios de RICA.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Ficha inexistente → 404 (en vez de un 500 genérico)
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
    }
}
