package com.example.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.backend.model.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {
    List<Evento> findByAsistentes_Id(Long usuarioId);

    boolean existsByOrganizadorId(Long usuarioId);

    boolean existsByAsistentes_Id(Long usuarioId);

    @Override
    @EntityGraph(attributePaths = { "ubicacion", "categoria", "organizador" })
    Page<Evento> findAll(Specification<Evento> spec, Pageable pageable);
}
