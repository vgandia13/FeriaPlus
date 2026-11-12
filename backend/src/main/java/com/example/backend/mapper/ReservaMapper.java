package com.example.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.backend.DTO.ReservaDTO;
import com.example.backend.model.Reserva;

@Mapper(componentModel = "spring")
public interface ReservaMapper {
    @Mapping(source = "expositor.id", target = "expositorId")
    @Mapping(source = "puesto.id", target = "puestoId")
    ReservaDTO toDTO(Reserva reserva);
}
