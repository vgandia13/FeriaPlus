package com.example.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.backend.DTO.UsuarioRegistroDTO;
import com.example.backend.DTO.UsuarioResponseDTO;
import com.example.backend.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponseDTO toResponseDTO(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // la codifica el servicio: nunca copiarla en claro
    @Mapping(target = "fechaRegistro", ignore = true) // la pone @PrePersist
    Usuario toEntity(UsuarioRegistroDTO dto);
}
