package com.example.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

@Component
public class JwtUtils {

    private final int JWT_EXPIRATION_MS = 86400000; // 24 horas
    @Value("${jwt.secret}")
    private String JWT_SECRET;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    // Método para generar la "pulsera de acceso"
    // El subject es el id (no el email) para que cambiar el email no invalide el token
    public String generateToken(Long usuarioId) {

        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + JWT_EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    // Método para leer quién es el dueño del token
    public Long getUsuarioIdFromToken(String token) {
        return Long.valueOf(Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }

    // Método para verificar si el token es válido o ha sido manipulado
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            // Aquí podrías loguear si el token ha expirado o es falso
            return false;
        }
    }
}