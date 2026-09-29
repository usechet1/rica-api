package com.rica.ricaapi.comun;

public class ApiError {

    private final String mensaje;

    public ApiError(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}
