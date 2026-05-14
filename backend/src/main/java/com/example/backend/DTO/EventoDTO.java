package com.example.backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class EventoDTO {
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;
    
    @NotBlank(message = "La fecha es obligatoria")
    private String fecha; 
    @NotNull(message = "La ubicación es obligatoria")
    private UbicacionDTO ubicacion;
    private Double latitud;
    private Double longitud;
    private String imagenUrl;
    private Long categoriaId;
    
    @NotNull(message = "El ID del organizador es obligatorio")
    private Long organizadorId;
    
    private java.util.List<ResenaDTO> resenas;
}
