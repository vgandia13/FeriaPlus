package com.example.backend.mapper;

import org.mapstruct.Mapper;

import com.example.backend.DTO.CategoriaDTO;
import com.example.backend.model.Categoria;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    CategoriaDTO toDTO(Categoria categoria);
    Categoria toEntity(CategoriaDTO dto);
}
