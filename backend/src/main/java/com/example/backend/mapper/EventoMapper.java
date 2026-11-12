package com.example.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.backend.DTO.EventoDTO;
import com.example.backend.model.Evento;

// uses: para convertir ubicacion y resenas reutiliza esos mappers
@Mapper(componentModel = "spring", uses = {UbicacionMapper.class, ResenaMapper.class})
public interface EventoMapper {

    @Mapping(source = "ubicacion.latitud", target = "latitud")
    @Mapping(source = "ubicacion.longitud", target = "longitud")
    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "organizador.id", target = "organizadorId")
    EventoDTO toDTO(Evento evento);

    // Versión corta para el dashboard: sin ubicación ni reseñas (como antes de MapStruct)
    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "organizador.id", target = "organizadorId")
    @Mapping(target = "ubicacion", ignore = true)
    @Mapping(target = "latitud", ignore = true)
    @Mapping(target = "longitud", ignore = true)
    @Mapping(target = "resenas", ignore = true)
    EventoDTO toResumenDTO(Evento evento);

    // Las relaciones las pone el servicio: hay que buscarlas en la BD y lanzar 404 si no existen
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ubicacion", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "organizador", ignore = true)
    @Mapping(target = "asistentes", ignore = true)
    @Mapping(target = "resenas", ignore = true)
    @Mapping(target = "puestos", ignore = true)
    Evento toEntity(EventoDTO dto);
}
