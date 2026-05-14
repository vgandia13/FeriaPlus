package com.example.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private UsuarioResponseDTO usuario;
    private List<ResenaDTO> resenas;
    private List<ReservaDTO> reservas;
    private List<EventoDTO> eventosAsistidos;
    private List<PuestoDTO> puestos;
}
