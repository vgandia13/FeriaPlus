package com.example.backend.service;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.DTO.EventoDTO;
import com.example.backend.exception.AccesoDenegadoException;
import com.example.backend.exception.BusquedaInvalidaException;
import com.example.backend.exception.CategoriaNoEncontradaException;
import com.example.backend.exception.EventoNoEncontradoException;
import com.example.backend.exception.OrdenInvalidoException;
import com.example.backend.exception.UbicacionNoEncontradaException;
import com.example.backend.exception.UsuarioNoEncontradoException;
import com.example.backend.mapper.EventoMapper;
import com.example.backend.model.Categoria;
import com.example.backend.model.Evento;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.EventoRepository;
import com.example.backend.repository.EventoSpecs;
import com.example.backend.repository.UbicacionRepository;
import com.example.backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final EventoMapper eventoMapper;
    private final UbicacionRepository ubicacionRepository;
    private static final Set<String> CAMPOS_ORDEN = Set.of("nombre", "fecha", "id");

    @Transactional(readOnly = true)
    public Page<EventoDTO> obtenerTodos(String nombre, Long categoriaId,
        Double lat, Double lng, Double radioKm, Pageable pageable) {
        for (Sort.Order orden : pageable.getSort()) {
            if (!CAMPOS_ORDEN.contains(orden.getProperty())) {
                throw new OrdenInvalidoException("Campo de ordenacion inválido");
            }
        }
        validarCercania(lat, lng, radioKm);
        Specification<Evento> filtros = Specification.allOf(
                EventoSpecs.nombreContiene(nombre),
                EventoSpecs.deCategoria(categoriaId),
                EventoSpecs.cercaDe(lat, lng, radioKm));
        Page<Evento> eventos = eventoRepository.findAll(filtros, pageable);
        return eventos.map(eventoMapper::toDTO);
    }

    @Transactional
    public EventoDTO guardar(EventoDTO eventoDTO, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                                            .orElseThrow(() -> new UsuarioNoEncontradoException("No se ha encontrado el usuario"));
        Evento evento = mapToEntity(eventoDTO);
        evento.setOrganizador(usuario);
        Evento nuevoEvento = eventoRepository.save(evento);
        return eventoMapper.toDTO(nuevoEvento);
    }

    @Transactional 
    public EventoDTO editar(Long id, EventoDTO dto, String email){
        Evento evento = eventoRepository.findById(id)
                        .orElseThrow(() -> new EventoNoEncontradoException("No se ha encontrado el evento"));
        Usuario usuario = usuarioRepository.findByEmail(email)
                                            .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        boolean esAdmin = usuario.getRol() == Rol.ROLE_ADMIN;
        if (!esAdmin && (evento.getOrganizador() == null
                || !evento.getOrganizador().getEmail().equals(email))) {
            throw new AccesoDenegadoException("No puedes editar este evento");
        }
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(LocalDate.parse(dto.getFecha(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
        if(dto.getUbicacion() == null || dto.getUbicacion().getId() == null){
            throw new UbicacionNoEncontradaException("Falta la ubicacion");
        }
        evento.setUbicacion(ubicacionRepository.findById(dto.getUbicacion().getId())
            .orElseThrow(() -> new UbicacionNoEncontradaException("Ubicación no encontrada")));
        evento.setCategoria(buscarCategoria(dto.getCategoriaId()));
        return eventoMapper.toDTO(eventoRepository.save(evento));
    }

    @Transactional(readOnly = true)
    public EventoDTO obtenerPorId(Long id) {
        return eventoRepository.findById(id)
                .map(eventoMapper::toDTO)
                .orElseThrow(() -> new EventoNoEncontradoException("Evento no encontrado"));
    }

    @Transactional
    public void inscribirUsuario(Long eventoId, String emailUsuario) {
        Evento e = eventoRepository.findById(eventoId).orElseThrow(() -> new EventoNoEncontradoException("Evento no encontrado"));
        Usuario u = usuarioRepository.findByEmail(emailUsuario).orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        e.getAsistentes().add(u);
        eventoRepository.save(e);
    }

    private Evento mapToEntity(EventoDTO dto) {
        Evento evento = eventoMapper.toEntity(dto);
        if (dto.getUbicacion() == null || dto.getUbicacion().getId() == null) {
            throw new UbicacionNoEncontradaException("Falta la ubicacion");
        }
        evento.setUbicacion(ubicacionRepository.findById(dto.getUbicacion().getId())
            .orElseThrow(() -> new UbicacionNoEncontradaException("Ubicación no encontrada")));
        evento.setCategoria(buscarCategoria(dto.getCategoriaId()));
        return evento;
    }

    // La categoría es opcional; si llega un id, tiene que existir
    private Categoria buscarCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoría no encontrada"));
    }

    private static final double RADIO_MAXIMO_KM = 500;

    // Los tres parámetros van juntos: o vienen todos o ninguno
    private void validarCercania(Double lat, Double lng, Double radioKm) {
        if (lat == null && lng == null && radioKm == null) {
            return;
        }
        if (lat == null || lng == null || radioKm == null) {
            throw new BusquedaInvalidaException("Para buscar cerca hacen falta lat, lng y radioKm");
        }
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new BusquedaInvalidaException("Coordenadas fuera de rango");
        }
        if (radioKm <= 0 || radioKm > RADIO_MAXIMO_KM) {
            throw new BusquedaInvalidaException("El radio debe estar entre 0 y " + (int) RADIO_MAXIMO_KM + " km");
        }
    }

}
