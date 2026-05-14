package com.example.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.model.Resena;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByUsuarioId(Long usuarioId);
}

