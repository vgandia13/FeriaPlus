package com.example.backend.service;

import com.example.backend.DTO.EventoDTO;
import com.example.backend.exception.NoAutorizadoException;
import com.example.backend.mapper.UbicacionMapper;
import com.example.backend.model.Evento;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock EventoRepository eventoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock UbicacionMapper ubicacionMapper;

    @InjectMocks EventoService eventoService;

    @Test
    void unOrganizadorNoEditaUnEventoDeOtro() {
        Usuario duenio = new Usuario();
        duenio.setEmail("duenio@test.com");
        duenio.setRol(Rol.ROLE_ORGANIZADOR);

        Usuario otro = new Usuario();
        otro.setEmail("otro@test.com");
        otro.setRol(Rol.ROLE_ORGANIZADOR);

        Evento evento = new Evento();
        evento.setId(1L);
        evento.setNombre("Original");
        evento.setOrganizador(duenio);

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(usuarioRepository.findByEmail("otro@test.com")).thenReturn(Optional.of(otro));

        EventoDTO cambios = new EventoDTO();
        cambios.setNombre("Hackeado");

        assertThatThrownBy(() -> eventoService.editar(1L, cambios, "otro@test.com"))
            .isInstanceOf(NoAutorizadoException.class);
        verify(eventoRepository, never()).save(any());
    }
}
