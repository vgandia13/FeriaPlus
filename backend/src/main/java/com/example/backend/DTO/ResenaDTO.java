package com.example.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class ResenaDTO {
    private Long id;
    private Integer valoracion;
    private String comentario;
    private Long ubicacionId;
    private Long eventoId;
}
