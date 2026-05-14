package com.example.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor @AllArgsConstructor
public class ReservaDTO {
    private Long id;
    private LocalDateTime fechaReserva;
    private String estado;
    private Long expositorId;
    private Long puestoId;
}
