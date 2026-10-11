package com.rica.investigadoresservice.comun;

import com.rica.investigadoresservice.investigadores.dominio.CorreoDuplicadoException;
import com.rica.investigadoresservice.investigadores.dominio.InvestigadorNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo de errores propio de investigadores-service. Es una copia adaptada
 * del que tenía rica-api: con servicios desplegados por separado ya no hay un
 * único @RestControllerAdvice compartido (ver NOTAS.md de rica-api).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Value Object (CorreoInstitucional) y otros argumentos inválidos → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
    }

    // Correo duplicado → 409 (conflicto con el estado actual)
    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarConflicto(CorreoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(ex.getMessage()));
    }

    // Investigador inexistente → 404
    @ExceptionHandler(InvestigadorNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(InvestigadorNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
    }
}
