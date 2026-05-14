package com.example.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.backend.mapper.UbicacionMapper;
import com.example.backend.DTO.EventoDTO;
import com.example.backend.DTO.ResenaDTO;
import com.example.backend.exception.EventoNoEncontradoException;
import com.example.backend.exception.UsuarioNoEncontradoException;
import com.example.backend.model.Evento;
import com.example.backend.model.Usuario;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.EventoRepository;
import com.example.backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final UbicacionMapper ubicacionMapper;

    public Page<EventoDTO> obtenerTodos(String nombre, Pageable pageable) {
        Page<Evento> eventos;
        if (nombre != null && !nombre.isBlank()) {
            eventos = eventoRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else {
            eventos = eventoRepository.findAll(pageable);
        }
        return eventos.map(this::mapToDTO);
    }

    public List<EventoDTO> listarPorCategoria(Long categoriaId) {
        return eventoRepository.findByCategoriaId(categoriaId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public EventoDTO guardar(EventoDTO eventoDTO) {
        Evento evento = mapToEntity(eventoDTO);
        Evento nuevoEvento = eventoRepository.save(evento);
        return mapToDTO(nuevoEvento);
    }

    public EventoDTO obtenerPorId(Long id) {
        return eventoRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EventoNoEncontradoException("Evento no encontrado"));
    }

    @Transactional
    public void inscribirUsuario(Long eventoId, String emailUsuario) {
        Evento e = eventoRepository.findById(eventoId).orElseThrow(() -> new EventoNoEncontradoException("Evento no encontrado"));
        Usuario u = usuarioRepository.findByEmail(emailUsuario).orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        e.getAsistentes().add(u);
        eventoRepository.save(e);
    }


    private EventoDTO mapToDTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setNombre(evento.getNombre());
        dto.setDescripcion(evento.getDescripcion());
        dto.setFecha(evento.getFecha() != null ? evento.getFecha().toString() : null);
        if (evento.getUbicacion() != null) {
            dto.setUbicacion(ubicacionMapper.ubicacionToUbicacionDTO(evento.getUbicacion()));
            dto.setLatitud(evento.getUbicacion().getLatitud());
            dto.setLongitud(evento.getUbicacion().getLongitud());
        } else {
            dto.setUbicacion(null);
            dto.setLatitud(null);
            dto.setLongitud(null);
        }
        dto.setImagenUrl(evento.getImagenUrl());
        
        if(evento.getCategoria() != null){
            dto.setCategoriaId(evento.getCategoria().getId());
        }
        if(evento.getOrganizador() != null){
            dto.setOrganizadorId(evento.getOrganizador().getId());
        }
        if (evento.getReseñas() != null) {
            dto.setResenas(evento.getReseñas().stream()
                    .map(r -> new ResenaDTO(r.getId(), r.getValoracion(), r.getComentario(), 
                                           r.getUbicacion() != null ? r.getUbicacion().getId() : null, 
                                           r.getEvento() != null ? r.getEvento().getId() : null))
                    .toList());
        }
        return dto;
    }

    private Evento mapToEntity(EventoDTO dto) {
        Evento evento = new Evento();
        evento.setId(dto.getId());
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        if(dto.getFecha() != null){
            evento.setFecha(LocalDate.parse(dto.getFecha()));
        }
        evento.setUbicacion(dto.getUbicacion() != null ? ubicacionMapper.ubicacionDTOToUbicacion(dto.getUbicacion()) : null);
        evento.setImagenUrl(dto.getImagenUrl());

        if(dto.getCategoriaId() != null){
            evento.setCategoria(categoriaRepository.findById(dto.getCategoriaId()).orElse(null));
        }
        if(dto.getOrganizadorId() != null){
            evento.setOrganizador(usuarioRepository.findById(dto.getOrganizadorId()).orElse(null));
        }
        return evento;
    }
}
