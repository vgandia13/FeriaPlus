package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils;

    // Inyectamos nuestras herramientas
    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Obtener el token del header "Authorization"
        String header = request.getHeader("Authorization");
        String token = null;
        Long usuarioId = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7); // Quitamos "Bearer " para quedarnos solo con el código
            try {
                if (jwtUtils.validateToken(token)) {
                    usuarioId = jwtUtils.getUsuarioIdFromToken(token);
                }
            } catch (Exception e) {
                // Token corrupto: seguimos sin autenticar y decide SecurityConfig
            }
        }

        // Si hay id y el usuario no está ya autenticado en esta petición...
        if (usuarioId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserById(usuarioId);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException e) {
                // Usuario borrado: seguimos sin autenticar
            }
        }

        // Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}