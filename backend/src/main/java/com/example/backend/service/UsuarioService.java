package com.example.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.backend.repository.UsuarioRepository;
import com.example.backend.model.Usuario;
import com.example.backend.DTO.DashboardDTO;
import com.example.backend.DTO.ResenaDTO;
import com.example.backend.DTO.ReservaDTO;
import com.example.backend.DTO.EventoDTO;
import com.example.backend.DTO.PuestoDTO;
import com.example.backend.repository.ResenaRepository;
import com.example.backend.repository.ReservaRepository;
import com.example.backend.repository.EventoRepository;
import com.example.backend.repository.PuestoRepository;
import java.util.stream.Collectors;
import com.example.backend.DTO.UsuarioResponseDTO;
import com.example.backend.exception.EmailRegistradoException;
import com.example.backend.exception.UsuarioNoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ResenaRepository resenaRepository;
    private final ReservaRepository reservaRepository;
    private final EventoRepository eventoRepository;
    private final PuestoRepository puestoRepository;

    public UsuarioResponseDTO registrarUsuario(UsuarioRegistroDTO registroDTO){
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new EmailRegistradoException("El email ya está registrado");
        }

        Usuario usuario = mapToEntity(registroDTO);
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return mapToResponseDTO(savedUsuario);
    }

    public Page<UsuarioResponseDTO> listarUsuarios(String nombre, Pageable pageable) {
        Page<Usuario> usuarios;
        if(nombre != null && !nombre.isBlank()) {
            usuarios = usuarioRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else {
            usuarios = usuarioRepository.findAll(pageable);
        }
        return usuarios.map(this::mapToResponseDTO);
    }

    public UsuarioResponseDTO obtenerPerfilPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        return mapToResponseDTO(usuario);
    }

    public DashboardDTO obtenerDatosDashboard(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setUsuario(mapToResponseDTO(usuario));

        dashboard.setResenas(resenaRepository.findByUsuarioId(usuario.getId()).stream()
                .map(r -> new ResenaDTO(r.getId(), r.getValoracion(), r.getComentario(), r.getUbicacion().getId(), r.getEvento().getId()))
                .collect(Collectors.toList()));

        dashboard.setReservas(reservaRepository.findByExpositorId(usuario.getId()).stream()
                .map(r -> new ReservaDTO(r.getId(), r.getFechaReserva(), r.getEstado().toString(), r.getExpositor().getId(), r.getPuesto().getId()))
                .collect(Collectors.toList()));

        // eventos asistidos
        dashboard.setEventosAsistidos(eventoRepository.findByAsistentes_Id(usuario.getId()).stream()
                .map(e -> new EventoDTO(e.getId(), e.getNombre(), e.getDescripcion(), e.getFecha().toString(), null, null, null, e.getImagenUrl(), e.getCategoria().getId(), e.getOrganizador().getId()))
                .collect(Collectors.toList()));

        return dashboard;
    }

    // Método para que el usuario actualice su propio perfil (sin poder cambiar su rol)
    public UsuarioResponseDTO actualizarPerfilPorUsername(String email, UsuarioRegistroDTO actualizacionDTO) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 1. Verificación de cambio de email
        if (actualizacionDTO.getEmail() != null && !usuario.getEmail().equals(actualizacionDTO.getEmail())) {
            if (usuarioRepository.existsByEmail(actualizacionDTO.getEmail())) {
                throw new EmailRegistradoException("El email ya está registrado");
            }
            usuario.setEmail(actualizacionDTO.getEmail());
        }

        // 2. Actualización de datos básicos
        if (actualizacionDTO.getNombre() != null && !actualizacionDTO.getNombre().isBlank()) {
            usuario.setNombre(actualizacionDTO.getNombre());
        }

        // 3. Actualización de contraseña (solo si se envía una nueva)
        if (actualizacionDTO.getPassword() != null && !actualizacionDTO.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(actualizacionDTO.getPassword()));
        }

        // IMPORTANTE: No incluimos usuario.setRol() aquí por seguridad.

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return mapToResponseDTO(updatedUsuario);
    }

    public UsuarioResponseDTO actualizarPerfil(Long id, UsuarioRegistroDTO actualizacionDTO){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        if (!usuario.getEmail().equals(actualizacionDTO.getEmail()) && usuarioRepository.existsByEmail(actualizacionDTO.getEmail())) {
            throw new EmailRegistradoException("El email ya está registrado");
        }
        usuario.setNombre(actualizacionDTO.getNombre());
        usuario.setEmail(actualizacionDTO.getEmail());
        usuario.setRol(actualizacionDTO.getRol());

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return mapToResponseDTO(updatedUsuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO mapToResponseDTO(Usuario usuario) {
        UsuarioResponseDTO responseDTO = new UsuarioResponseDTO();
        responseDTO.setId(usuario.getId());
        responseDTO.setNombre(usuario.getNombre());
        responseDTO.setEmail(usuario.getEmail());
        responseDTO.setRol(usuario.getRol());
        responseDTO.setFechaRegistro(usuario.getFechaRegistro());
        return responseDTO;
    }

    private Usuario mapToEntity(UsuarioRegistroDTO registroDTO) {
        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.getNombre());
        usuario.setEmail(registroDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        usuario.setRol(registroDTO.getRol());
        return usuario;
    }
}
