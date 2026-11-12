package com.example.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.backend.DTO.ResenaDTO;
import com.example.backend.model.Resena;

@Mapper(componentModel = "spring")
public interface ResenaMapper {
    @Mapping(source = "ubicacion.id", target = "ubicacionId")
    @Mapping(source = "evento.id", target = "eventoId")
    ResenaDTO toDTO(Resena resena);
}
