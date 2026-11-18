package com.inmobiliaria.proyecto.exception;

public class CorreoEnUsoException extends RuntimeException {

    public CorreoEnUsoException(String mensaje) {
        super(mensaje);
    }
}
