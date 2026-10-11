package com.rica.publicacionesservice.comun;

import com.rica.publicacionesservice.publicaciones.LimiteAnualExcedidoException;
import com.rica.publicacionesservice.publicaciones.ServicioNoDisponibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo de errores propio de publicaciones-service. Es una copia adaptada
 * del que tenía rica-api: con servicios desplegados por separado ya no hay un
 * único @RestControllerAdvice compartido (ver NOTAS.md de rica-api).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
    }

    // Límite anual excedido → 409 (conflicto con el estado actual)
    @ExceptionHandler(LimiteAnualExcedidoException.class)
    public ResponseEntity<ApiError> manejarConflicto(LimiteAnualExcedidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(ex.getMessage()));
    }

    // Investigador inexistente (según VerificadorInvestigador) → 404
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
    }

    // investigadores-service caído o circuito abierto → 503
    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ApiError> manejarServicioNoDisponible(ServicioNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiError(ex.getMessage()));
    }
}
