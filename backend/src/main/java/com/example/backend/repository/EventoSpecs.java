package com.example.backend.repository;

import org.springframework.data.jpa.domain.Specification;

import com.example.backend.model.Evento;

// Filtros opcionales de la búsqueda de eventos: cada uno devuelve null si no se usa,
// y Spring Data ignora los null al combinarlos con Specification.allOf(...)
public final class EventoSpecs {

    private EventoSpecs() {
    }

    public static Specification<Evento> nombreContiene(String nombre) {
        return (root, query, cb) -> {
            if (nombre == null || nombre.isBlank()) {
                return null;
            }
            // Escapa \ % y _ para que el usuario no pueda usarlos como comodines
            String patron = "%" + nombre.toLowerCase()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            return cb.like(cb.lower(root.get("nombre")), patron, '\\');
        };
    }

    public static Specification<Evento> deCategoria(Long categoriaId) {
        return (root, query, cb) -> categoriaId == null
                ? null
                : cb.equal(root.get("categoria").get("id"), categoriaId);
    }
}
