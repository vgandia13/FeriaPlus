package com.example.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.DTO.DashboardDTO;
import com.example.backend.DTO.UsuarioActualizacionDTO;
import com.example.backend.DTO.UsuarioAdminUpdateDTO;
import com.example.backend.DTO.UsuarioRegistroDTO;
import com.example.backend.DTO.UsuarioResponseDTO;
import com.example.backend.exception.AccesoDenegadoException;
import com.example.backend.exception.EmailRegistradoException;
import com.example.backend.exception.UsuarioConDatosException;
import com.example.backend.exception.UsuarioNoEncontradoException;
import com.example.backend.mapper.EventoMapper;
import com.example.backend.mapper.PuestoMapper;
import com.example.backend.mapper.ResenaMapper;
import com.example.backend.mapper.ReservaMapper;
import com.example.backend.mapper.UsuarioMapper;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.repository.EventoRepository;
import com.example.backend.repository.PuestoRepository;
import com.example.backend.repository.ResenaRepository;
import com.example.backend.repository.ReservaRepository;
import com.example.backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResenaRepository resenaRepository;
    private final ReservaRepository reservaRepository;
    private final EventoRepository eventoRepository;
    private final PuestoRepository puestoRepository;
    private final UsuarioMapper usuarioMapper;
    private final ResenaMapper resenaMapper;
    private final ReservaMapper reservaMapper;
    private final EventoMapper eventoMapper;
    private final PuestoMapper puestoMapper;

    public UsuarioResponseDTO registrarUsuario(UsuarioRegistroDTO registroDTO) {
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new EmailRegistradoException("El email ya está registrado");
        }

        Usuario usuario = mapToEntity(registroDTO);
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(savedUsuario);
    }

    public Page<UsuarioResponseDTO> listarUsuarios(String nombre, Pageable pageable) {
        Page<Usuario> usuarios;
        if (nombre != null && !nombre.isBlank()) {
            usuarios = usuarioRepository.findByNombreContainingIgnoreCaseAndRolNot(nombre, Rol.ROLE_ADMIN, pageable);
        } else {
            usuarios = usuarioRepository.findByRolNot(Rol.ROLE_ADMIN, pageable);
        }
        return usuarios.map(usuarioMapper::toResponseDTO);
    }

    public UsuarioResponseDTO obtenerPerfilPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO obtenerUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        return usuarioMapper.toResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public DashboardDTO obtenerDatosDashboard(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setUsuario(usuarioMapper.toResponseDTO(usuario));

        dashboard.setResenas(resenaRepository.findByUsuarioId(usuario.getId()).stream()
            .map(resenaMapper::toDTO).toList());
        dashboard.setReservas(reservaRepository.findByExpositorId(usuario.getId()).stream()
            .map(reservaMapper::toDTO).toList());
        dashboard.setEventosAsistidos(eventoRepository.findByAsistentes_Id(usuario.getId()).stream()
            .map(eventoMapper::toResumenDTO).toList());
        dashboard.setPuestos(puestoRepository.findPuestosByExpositorId(usuario.getId()).stream()
            .map(puestoMapper::toDTO).toList());
            
        return dashboard;
    }

    // Método para que el usuario actualice su propio perfil (sin poder cambiar su
    // rol)
    public UsuarioResponseDTO actualizarPerfilPorUsername(String email, UsuarioActualizacionDTO actualizacionDTO) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 1. Verificación de cambio de email
        if (actualizacionDTO.getEmail() != null && !usuario.getEmail().equals(actualizacionDTO.getEmail())) {
            if (usuarioRepository.existsByEmail(actualizacionDTO.getEmail())) {
                throw new EmailRegistradoException("El email ya está registrado");
            }
            if(actualizacionDTO.getOldPassword() == null 
                || !passwordEncoder.matches(actualizacionDTO.getOldPassword(), usuario.getPassword()) ) {
                throw new AccesoDenegadoException("La contraseña no es correcta");
            }
            usuario.setEmail(actualizacionDTO.getEmail());
        }

        // 2. Actualización de datos básicos
        if (actualizacionDTO.getNombre() != null && !actualizacionDTO.getNombre().isBlank()) {
            usuario.setNombre(actualizacionDTO.getNombre());
        }

        // 3. Actualización de contraseña (solo si se envía una nueva)
        if (actualizacionDTO.getNewPassword() != null && !actualizacionDTO.getNewPassword().isBlank()) {
            if (actualizacionDTO.getOldPassword() == null
                || !passwordEncoder.matches(actualizacionDTO.getOldPassword(), usuario.getPassword())) {
                throw new AccesoDenegadoException("La contraseña actual no es correcta");
            }
            usuario.setPassword(passwordEncoder.encode(actualizacionDTO.getNewPassword()));
        }

        // IMPORTANTE: No incluimos usuario.setRol() aquí por seguridad.

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(updatedUsuario);
    }

    public UsuarioResponseDTO actualizarPerfil(Long id, UsuarioAdminUpdateDTO actualizacionDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        // Un admin no puede editar a ningún admin, tampoco a sí mismo
        if (usuario.getRol() == Rol.ROLE_ADMIN) {
            throw new AccesoDenegadoException("No puedes modificar a un administrador");
        }
        if (actualizacionDTO.getRol() == Rol.ROLE_ADMIN) {
            throw new AccesoDenegadoException("No puedes asignar el rol de administrador");
        }
        if (!usuario.getEmail().equals(actualizacionDTO.getEmail())
                && usuarioRepository.existsByEmail(actualizacionDTO.getEmail())) {
            throw new EmailRegistradoException("El email ya está registrado");
        }
        usuario.setNombre(actualizacionDTO.getNombre());
        usuario.setEmail(actualizacionDTO.getEmail());
        usuario.setRol(actualizacionDTO.getRol());

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(updatedUsuario);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException("No existe usuario con ese ID"));
        if (usuario.getRol() == Rol.ROLE_ADMIN) {
            throw new AccesoDenegadoException("No puedes eliminar a un administrador");
        }
        if(eventoRepository.existsByAsistentes_Id(id) || eventoRepository.existsByOrganizadorId(id) || reservaRepository.existsByExpositorId(id) || resenaRepository.existsByUsuarioId(id)){
            throw new UsuarioConDatosException("No se puede eliminar: el usuario tiene eventos, reservas o reseñas");
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario mapToEntity(UsuarioRegistroDTO registroDTO) {
        if (registroDTO.getRol() == Rol.ROLE_ADMIN) {
            throw new AccesoDenegadoException("No se puede registrar como Admin");
        }
        Usuario usuario = usuarioMapper.toEntity(registroDTO);
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        return usuario;
    }

}
