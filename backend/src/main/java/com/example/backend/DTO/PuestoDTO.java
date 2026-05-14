package com.example.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class PuestoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
}
