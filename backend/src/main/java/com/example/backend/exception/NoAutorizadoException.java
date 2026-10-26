package com.example.backend.exception;

public class NoAutorizadoException  extends RuntimeException {
    public NoAutorizadoException(String message) {
        super(message);
    }

}
