package com.rica.ricaapi.investigadores.dominio;

public class InvestigadorNoEncontradoException extends RuntimeException {

    public InvestigadorNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
