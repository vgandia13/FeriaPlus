package com.example.backend.DTO;

import java.util.List;

import lombok.Data;

@Data
public class UbicacionDTO {
    private String id;
    private String nombre;
    private Double latitud;
    private Double longitud;
    private List<EventoDTO> eventos;
}
