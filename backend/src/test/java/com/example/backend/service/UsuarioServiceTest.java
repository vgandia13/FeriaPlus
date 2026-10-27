package com.example.backend.service;

import com.example.backend.exception.UsuarioConDatosException;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ResenaRepository resenaRepository;
    @Mock ReservaRepository reservaRepository;
    @Mock EventoRepository eventoRepository;
    @Mock PuestoRepository puestoRepository;

    @InjectMocks UsuarioService usuarioService;

    @Test 
    void noBorrarUnUsuarioQueOrganizaEventos() {
        Usuario usuario = new Usuario();
        usuario.setRol(Rol.ROLE_ORGANIZADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(eventoRepository.existsByOrganizadorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.eliminarUsuario(1L))
            .isInstanceOf(UsuarioConDatosException.class);
        verify(usuarioRepository, never()).delete(any());
    }   
}
