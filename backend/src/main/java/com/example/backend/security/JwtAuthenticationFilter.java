package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    // Inyectamos nuestras herramientas
    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Obtener el token del header "Authorization"
        String header = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7); // Quitamos "Bearer " para quedarnos solo con el código
            try {
                if(jwtUtils.validateToken(token)) {
                    email = jwtUtils.getEmailFromToken(token);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
                    return;
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error al validar el token");
                return;
            }
        }

        //  Si hay email y el usuario no está ya autenticado en esta petición...
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String rolesString = jwtUtils.getRolesFromToken(token);
            
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        //  Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}