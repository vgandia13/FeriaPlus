package com.example.backend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.backend.model.Rol;

@Data
@NoArgsConstructor @AllArgsConstructor
public class UsuarioAdminUpdateDTO {
    @NotBlank
    private String nombre;

    @Email
    @NotBlank 
    private String email;

    @NotNull
    private Rol rol;
}
