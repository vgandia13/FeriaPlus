package com.example.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    @Test
    @SuppressWarnings("unchecked")
    void validacionConErrorDeClaseDa400YConservaLosMensajes() {
        var resultado = new BeanPropertyBindingResult(new Object(), "usuario");
        resultado.addError(new FieldError("usuario", "email", "El email es obligatorio"));
        resultado.addError(new ObjectError("usuario", "Las contraseñas no coinciden"));

        var respuesta = new GlobalExceptionHandler()
                .handleValidationExceptions(new MethodArgumentNotValidException(null, resultado));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var errores = (Map<String, String>) ((Map<String, Object>) respuesta.getBody()).get("errors");
        assertThat(errores)
                .containsEntry("email", "El email es obligatorio")
                .containsEntry("usuario", "Las contraseñas no coinciden");
    }
}
