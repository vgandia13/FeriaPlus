package com.example.backend.exception;

public class BusquedaInvalidaException extends RuntimeException{
    public BusquedaInvalidaException(String message) {
        super(message);
    }
}
