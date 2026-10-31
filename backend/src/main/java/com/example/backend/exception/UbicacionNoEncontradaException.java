package com.example.backend.exception;


public class UbicacionNoEncontradaException extends RuntimeException {
    public UbicacionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
