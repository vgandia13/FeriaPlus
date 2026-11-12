package com.example.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.backend.DTO.UbicacionDTO;
import com.example.backend.model.Ubicacion;

@Mapper(componentModel = "spring")
public interface UbicacionMapper {
    @Mapping(target = "eventos", ignore = true)
    UbicacionDTO ubicacionToUbicacionDTO(Ubicacion ubicacion);

    @Mapping(target = "eventos", ignore = true)
    Ubicacion ubicacionDTOToUbicacion(UbicacionDTO ubicacionDTO);
}
