package com.example.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.backend.DTO.LoginRequestDTO;
import com.example.backend.DTO.UsuarioRegistroDTO;
import com.example.backend.DTO.UsuarioResponseDTO;
import com.example.backend.security.JwtUtils;
import com.example.backend.service.UsuarioService;

import org.springframework.web.bind.annotation.PostMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        String token = jwtUtils.generateToken(authentication);

        UsuarioResponseDTO usuario = usuarioService.obtenerUsuarioPorEmail(loginRequest.getEmail());

        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("name", usuario.getNombre());
        usuarioMap.put("email", usuario.getEmail());
        usuarioMap.put("rol", usuario.getRol().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("usuario", usuarioMap);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody UsuarioRegistroDTO registroDTO) {
        UsuarioResponseDTO nuevoUsuario = usuarioService.registrarUsuario(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

}
