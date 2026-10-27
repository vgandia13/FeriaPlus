package com.example.backend.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByCategoriaId(Long categoriaId);
    Page<Evento> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    Page<Evento> findAll(Pageable pageable);
    List<Evento> findByAsistentes_Id(Long usuarioId);
    boolean existsByOrganizadorId(Long usuarioId);
    boolean existsByAsistentes_Id(Long usuarioId);
}
