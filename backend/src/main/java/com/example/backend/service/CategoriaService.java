package com.example.backend.service;

import org.springframework.stereotype.Service;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.DTO.CategoriaDTO;
import com.example.backend.model.Categoria;
import java.util.List;
import com.example.backend.exception.CategoriaNoEncontradaException;
import com.example.backend.mapper.CategoriaMapper;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    public List<CategoriaDTO> listarTodas(){
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toDTO)
                .toList();
    }

    public CategoriaDTO obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(categoriaMapper::toDTO)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoría no encontrada"));
    }

    public CategoriaDTO guardar(CategoriaDTO categoriaDTO) {
        Categoria categoria = categoriaMapper.toEntity(categoriaDTO);
        Categoria nuevaCategoria = categoriaRepository.save(categoria);
        return categoriaMapper.toDTO(nuevaCategoria);
    }
}
