package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.Puesto;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {
    @Query("SELECT p FROM Puesto p JOIN Reserva r ON r.puesto.id = p.id WHERE r.expositor.id = :usuarioId")
    List<Puesto> findPuestosByExpositorId(@Param("usuarioId") Long usuarioId);
}
