package com.example.backend.exception;

public class OrdenInvalidoException extends RuntimeException {
    public OrdenInvalidoException(String message) {
        super(message);
    }
}
