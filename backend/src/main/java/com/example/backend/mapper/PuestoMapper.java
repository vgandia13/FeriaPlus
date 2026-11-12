package com.example.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.backend.DTO.PuestoDTO;
import com.example.backend.model.Puesto;

@Mapper(componentModel = "spring")
public interface PuestoMapper {
    @Mapping(source = "dimensiones", target = "descripcion")
    PuestoDTO toDTO(Puesto puesto);
}