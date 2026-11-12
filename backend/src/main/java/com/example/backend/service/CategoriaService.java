package com.example.backend.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.CategoriaDTO;
import com.example.backend.mapper.CategoriaMapper;
import com.example.backend.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    public List<CategoriaDTO> listarTodas(){
        return categoriaRepository.findAll(Sort.by("nombre")).stream()
                .map(categoriaMapper::toDTO)
                .toList();
    }
}
