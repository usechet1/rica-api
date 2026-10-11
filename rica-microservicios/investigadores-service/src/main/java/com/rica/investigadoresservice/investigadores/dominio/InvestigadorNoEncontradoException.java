package com.rica.investigadoresservice.investigadores.dominio;

public class InvestigadorNoEncontradoException extends RuntimeException {

    public InvestigadorNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
