package com.rica.ricaapi.comun;

import com.rica.ricaapi.investigadores.dominio.CorreoDuplicadoException;
import com.rica.ricaapi.investigadores.dominio.InvestigadorNoEncontradoException;
import com.rica.ricaapi.publicaciones.LimiteAnualExcedidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones de dominio a respuestas HTTP limpias, sin cambiar
 * el comportamiento observable de los endpoints (mismos casos de éxito de antes,
 * ahora con errores legibles en vez de un 500 genérico).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Value Object (CorreoInstitucional) y otros argumentos inválidos → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
    }

    // Correo duplicado / límite anual excedido → 409 (conflicto con el estado actual)
    @ExceptionHandler({CorreoDuplicadoException.class, LimiteAnualExcedidoException.class})
    public ResponseEntity<ApiError> manejarConflicto(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(ex.getMessage()));
    }

    // Investigador inexistente → 404
    @ExceptionHandler(InvestigadorNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(InvestigadorNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
    }
}
