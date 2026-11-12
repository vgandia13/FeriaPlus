package com.example.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtUtilsTest {
    // HS256 exige una clave de al menos 32 bytes
    static final String SECRET = "clave-de-test-de-al-menos-32-bytes!!";
    JwtUtils jwtUtils;

    @BeforeEach 
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "JWT_SECRET", SECRET);
    }

    @Test 
    void elTokenGuardaElIdDelUsuario() {
        var auth = new UsernamePasswordAuthenticationToken("x", null,
                List.of(new SimpleGrantedAuthority("ROLE_VISITANTE")));
        String token = jwtUtils.generateToken(auth, 42L);
        assertThat(jwtUtils.getUsuarioIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void tokenCaducadoNoEsValido() {
        String token = Jwts.builder().subject("1").expiration(new Date(0))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes())).compact();
        assertThat(jwtUtils.validateToken(token)).isFalse();
    }

    @Test 
    void tokenFirmadoConOtraClaveNoEsValido() {
        String otraClave = "otra-clave-distinta-de-32-bytes-o-mas";
        String token = Jwts.builder().subject("1")
                .signWith(Keys.hmacShaKeyFor(otraClave.getBytes())).compact();
        assertThat(jwtUtils.validateToken(token)).isFalse();
    }
}
