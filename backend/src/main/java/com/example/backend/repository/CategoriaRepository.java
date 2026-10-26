package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

}
