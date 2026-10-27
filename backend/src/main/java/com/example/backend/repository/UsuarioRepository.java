package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Boolean existsByEmail(String email);
    Page<Usuario> findByNombreContainingIgnoreCaseAndRolNot(String nombre, Rol rol, Pageable pageable);
    Page<Usuario> findByRolNot(Rol rol, Pageable pageable);
    Usuario findByNombre(String nombre);
}
