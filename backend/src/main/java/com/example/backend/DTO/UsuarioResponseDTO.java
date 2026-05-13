package com.example.backend.DTO;

import java.time.LocalDateTime;
import com.example.backend.model.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private LocalDateTime fechaRegistro;
}
