package com.example.backend.repository;

import org.springframework.data.jpa.domain.Specification;

import com.example.backend.model.Evento;
import com.example.backend.model.Ubicacion;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;

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

    private static final double RADIO_TIERRA_KM = 6371.0;

    // Eventos a menos de radioKm del punto (lat, lng), con la fórmula de Haversine
    // en su forma de "ley de los cosenos". En vez de calcular acos(x) <= radio/R
    // se compara x >= cos(radio/R), que es equivalente (acos es decreciente) y evita
    // que acos reciba 1.0000001 por redondeo y devuelva NULL justo en el propio punto.
    public static Specification<Evento> cercaDe(Double lat, Double lng, Double radioKm) {
        return (root, query, cb) -> {
            if (lat == null || lng == null || radioKm == null) {
                return null;
            }
            Join<Evento, Ubicacion> ubicacion = root.join("ubicacion");
            Expression<Double> latEvento = enRadianes(cb, ubicacion.get("latitud"));
            Expression<Double> lngEvento = enRadianes(cb, ubicacion.get("longitud"));
            double latUsuario = Math.toRadians(lat);
            double lngUsuario = Math.toRadians(lng);

            // cos(c) = sin(lat1)·sin(lat2) + cos(lat1)·cos(lat2)·cos(lng2 - lng1)
            Expression<Double> cosAngulo = cb.sum(
                    cb.prod(Math.sin(latUsuario), seno(cb, latEvento)),
                    cb.prod(cb.prod(Math.cos(latUsuario), coseno(cb, latEvento)),
                            coseno(cb, cb.diff(lngEvento, lngUsuario))));
            return cb.greaterThanOrEqualTo(cosAngulo, Math.cos(radioKm / RADIO_TIERRA_KM));
        };
    }

    private static Expression<Double> enRadianes(CriteriaBuilder cb, Expression<Double> grados) {
        return cb.prod(grados, Math.PI / 180);
    }

    private static Expression<Double> seno(CriteriaBuilder cb, Expression<Double> x) {
        return cb.function("sin", Double.class, x);
    }

    private static Expression<Double> coseno(CriteriaBuilder cb, Expression<Double> x) {
        return cb.function("cos", Double.class, x);
    }

}
