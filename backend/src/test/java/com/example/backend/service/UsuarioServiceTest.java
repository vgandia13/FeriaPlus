package com.example.backend.service;

import com.example.backend.exception.AccesoDenegadoException;
import com.example.backend.exception.UsuarioConDatosException;
import com.example.backend.mapper.EventoMapper;
import com.example.backend.mapper.PuestoMapper;
import com.example.backend.mapper.ResenaMapper;
import com.example.backend.mapper.ReservaMapper;
import com.example.backend.mapper.UsuarioMapper;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.model.Resena;
import com.example.backend.DTO.DashboardDTO;
import com.example.backend.DTO.UsuarioAdminUpdateDTO;
import com.example.backend.DTO.UsuarioActualizacionDTO;
import com.example.backend.exception.EmailRegistradoException;
import com.example.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
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

    // Mappers reales donde el test comprueba lo que devuelven; el resto, mocks
    @Spy UsuarioMapper usuarioMapper = Mappers.getMapper(UsuarioMapper.class);
    @Spy ResenaMapper resenaMapper = Mappers.getMapper(ResenaMapper.class);
    @Mock ReservaMapper reservaMapper;
    @Mock EventoMapper eventoMapper;
    @Mock PuestoMapper puestoMapper;

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
    
    @Test 
    void dashboardConResenaSinUbicacionNoFalla() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("a@test.com");
        Resena resena = new Resena();
        resena.setUsuario(usuario);
        resena.setId(5L);

        when(usuarioRepository.findByEmail("a@test.com")).thenReturn(Optional.of(usuario));
        when(resenaRepository.findByUsuarioId(1L)).thenReturn(List.of(resena));

        DashboardDTO dashboard = usuarioService.obtenerDatosDashboard("a@test.com");
        
        assertThat(dashboard.getResenas().get(0).getUbicacionId())
            .isNull();

        assertThat(dashboard.getResenas().get(0).getEventoId())
            .isNull();
    }

    @Test
    void noSePuedeAsignarRolAdmin() {
        Usuario usuario = new Usuario();
        usuario.setRol(Rol.ROLE_VISITANTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioAdminUpdateDTO dto = new UsuarioAdminUpdateDTO();
        dto.setRol(Rol.ROLE_ADMIN);

        assertThatThrownBy(() -> usuarioService.actualizarPerfil(1L, dto))
            .isInstanceOf(AccesoDenegadoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noSePuedeEditarAUnAdmin() {
        Usuario admin = new Usuario();
        admin.setRol(Rol.ROLE_ADMIN);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> usuarioService.actualizarPerfil(1L, new UsuarioAdminUpdateDTO()))
            .isInstanceOf(AccesoDenegadoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarContrasenaConLaActualCorrectaGuardaLaNuevaCodificada() {
        Usuario usuario = new Usuario();
        usuario.setEmail("a@test.com");
        usuario.setPassword("hashVieja");
        when(usuarioRepository.findByEmail("a@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("vieja", "hashVieja")).thenReturn(true);
        when(passwordEncoder.encode("nueva")).thenReturn("hashNueva");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioActualizacionDTO dto = new UsuarioActualizacionDTO();
        dto.setOldPassword("vieja");
        dto.setNewPassword("nueva");

        usuarioService.actualizarPerfilPorUsername("a@test.com", dto);

        assertThat(usuario.getPassword()).isEqualTo("hashNueva");
    }

    @Test
    void cambiarAEmailYaRegistradoLanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setEmail("a@test.com");
        when(usuarioRepository.findByEmail("a@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("b@test.com")).thenReturn(true);

        UsuarioActualizacionDTO dto = new UsuarioActualizacionDTO();
        dto.setEmail("b@test.com");

        assertThatThrownBy(() -> usuarioService.actualizarPerfilPorUsername("a@test.com", dto))
            .isInstanceOf(EmailRegistradoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void eliminarUsuarioSinDatosLoBorra() {
        Usuario usuario = new Usuario();
        usuario.setRol(Rol.ROLE_VISITANTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminarUsuario(1L);

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void noBorrarUnUsuarioConReservas() {
        Usuario usuario = new Usuario();
        usuario.setRol(Rol.ROLE_EXPOSITOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(reservaRepository.existsByExpositorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.eliminarUsuario(1L))
            .isInstanceOf(UsuarioConDatosException.class);
        verify(usuarioRepository, never()).delete(any());
    }
}
