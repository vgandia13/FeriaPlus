package com.example.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import java.security.Principal;

import com.example.backend.DTO.DashboardDTO;
import com.example.backend.DTO.UsuarioActualizacionDTO;
import com.example.backend.DTO.UsuarioResponseDTO;
import com.example.backend.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardDTO> obtenerDashboard(Principal principal) {
        return ResponseEntity.ok(usuarioService.obtenerDatosDashboard(principal.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponseDTO> actualizarMiPerfil(
            Principal principal,
            @Valid @RequestBody UsuarioActualizacionDTO actualizacionDTO) {
            
        String email = principal.getName();
        return ResponseEntity.ok(usuarioService.actualizarPerfilPorUsername(email, actualizacionDTO));
    }
}