package com.example.backend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor @AllArgsConstructor
public class UsuarioActualizacionDTO {
    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String oldPassword;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String newPassword;
}
