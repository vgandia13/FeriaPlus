package com.example.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.example.backend.model.Ubicacion;
import com.example.backend.DTO.UbicacionDTO;

@Mapper(componentModel = "spring")
public interface UbicacionMapper {
    UbicacionMapper INSTANCE = Mappers.getMapper(UbicacionMapper.class);

    @Mapping(target = "eventos", ignore = true)
    UbicacionDTO ubicacionToUbicacionDTO(Ubicacion ubicacion);

    @Mapping(target = "eventos", ignore = true)
    Ubicacion ubicacionDTOToUbicacion(UbicacionDTO ubicacionDTO);
}
