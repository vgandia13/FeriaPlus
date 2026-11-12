package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.backend.model.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {
    List<Evento> findByAsistentes_Id(Long usuarioId);
    boolean existsByOrganizadorId(Long usuarioId);
    boolean existsByAsistentes_Id(Long usuarioId);
}
